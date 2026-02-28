package org.tron.common.utils;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.crypto.Hash;
import org.tron.common.exception.EncodingException;
import org.tron.service.eventactuator.SignListParam;

@Slf4j(topic = "abiUtil")
public class AbiUtil {

  private static Pattern paramTypeBytes = Pattern.compile("^bytes([0-9]*)$");
  private static Pattern paramTypeNumber = Pattern.compile("^(u?int)([0-9]*)$");
  private static Pattern paramTypeArray = Pattern.compile("^(.*)\\[([0-9]*)\\]$");

  //
  private static abstract class Coder {

    boolean dynamic = false;

    abstract byte[] encode(String value) throws EncodingException;

    abstract byte[] decode();

  }

  public static String[] getTypes(String methodSign) {
    int start = methodSign.indexOf('(') + 1;
    int end = methodSign.indexOf(')');

    String typeString = methodSign.subSequence(start, end).toString();

    return typeString.split(",");
  }

  public static Coder getParamCoder(String type) {

    switch (type) {
      case "address":
        return new CoderAddress();
      case "string":
        return new CoderString();
      case "bool":
        return new CoderBool();
      case "bytes":
        return new CoderDynamicBytes();
      case "trcToken":
        return new CoderNumber();
    }

    if (paramTypeBytes.matcher(type).find()) {
      return new CoderFixedBytes();
    }

    if (paramTypeNumber.matcher(type).find()) {
      return new CoderNumber();
    }

    Matcher m = paramTypeArray.matcher(type);
    if (m.find()) {
      String arrayType = m.group(1);
      int length = -1;
      if (!m.group(2).equals("")) {
        length = Integer.valueOf(m.group(2));
      }
      return new CoderArray(arrayType, length);
    }
    return null;
  }

  static class CoderArray extends Coder {

    private String elementType;
    private int length;

    CoderArray(String arrayType, int length) {
      this.elementType = arrayType;
      this.length = length;
      if (length == -1) {
        this.dynamic = true;
      }
      this.dynamic = true;
    }

    @Override
    byte[] encode(String arrayValues) throws EncodingException {

      Coder coder = getParamCoder(elementType);

      List<Object> strings = null;
      try {
        ObjectMapper mapper = new ObjectMapper();
        strings = mapper.readValue(arrayValues, List.class);
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }

      List<Coder> coders = new ArrayList<>();

      if (this.length == -1) {
        for (int i = 0; i < strings.size(); i++) {
          coders.add(coder);
        }
      } else {
        for (int i = 0; i < this.length; i++) {
          coders.add(coder);
        }
      }

      if (this.length == -1) {
        return ByteUtil
            .merge(new DataWord(strings.size()).getData(), pack(coders, strings));
      } else {
        return pack(coders, strings);
      }
    }

    @Override
    byte[] decode() {
      return new byte[0];
    }
  }

  static class CoderNumber extends Coder {

    @Override
    byte[] encode(String value) {
      BigInteger n = new BigInteger(value, 10);
      DataWord word = new DataWord(n.abs().toByteArray());
      return word.getData();
    }

    @Override
    byte[] decode() {
      return new byte[0];
    }
  }

  static class CoderFixedBytes extends Coder {

    @Override
    byte[] encode(String value) {

      if (value.startsWith("0x")) {
        value = value.substring(2);
      }

      if (value.length() % 2 != 0) {
        value = "0" + value;
      }

      byte[] result = new byte[32];
      byte[] bytes = Hex.decode(value);
      System.arraycopy(bytes, 0, result, 0, bytes.length);
      return result;
    }

    @Override
    byte[] decode() {
      return new byte[0];
    }
  }

  static class CoderToken extends Coder {

    @Override
    byte[] encode(String value) {
      String hex = Hex.toHexString(new DataWord(value.getBytes()).getData());
      return new CoderFixedBytes().encode(hex);
    }

    @Override
    byte[] decode() {
      return new byte[0];
    }
  }

  static class CoderDynamicBytes extends Coder {

    CoderDynamicBytes() {
      dynamic = true;
    }

    @Override
    byte[] encode(String value) {
      return encodeDynamicBytes(value);
    }

    @Override
    byte[] decode() {
      return new byte[0];
    }
  }

  static class CoderBool extends Coder {

    @Override
    byte[] encode(String value) {
      if (value.equals("true") || value.equals("1")) {
        return new DataWord(1).getData();
      } else {
        return new DataWord(0).getData();
      }

    }

    @Override
    byte[] decode() {
      return new byte[0];
    }
  }

  static class CoderAddress extends Coder {

    @Override
    byte[] encode(String value) throws EncodingException {
      byte[] address = WalletUtil.decodeFromBase58Check(value);
      if (address == null) {
        throw new EncodingException("invalid address input");
      }
      return new DataWord(address).getData();
    }

    @Override
    byte[] decode() {
      return new byte[0];
    }
  }

  static class CoderString extends Coder {

    CoderString() {
      dynamic = true;
    }

    @Override
    byte[] encode(String value) {
      return encodeString(value);
    }

    @Override
    byte[] decode() {
      return new byte[0];
    }
  }

  public static byte[] encodeString(String value) {
    byte[] data = value.getBytes();
    List<DataWord> ret = new ArrayList<>();
    ret.add(new DataWord(data.length));

    int readInx = 0;
    int len = value.getBytes().length;
    while (readInx < value.getBytes().length) {
      byte[] wordData = new byte[32];
      int readLen = len - readInx >= 32 ? 32 : (len - readInx);
      System.arraycopy(data, readInx, wordData, 0, readLen);
      DataWord word = new DataWord(wordData);
      ret.add(word);
      readInx += 32;
    }

    byte[] retBytes = new byte[ret.size() * 32];
    int retIndex = 0;

    for (DataWord w : ret) {
      System.arraycopy(w.getData(), 0, retBytes, retIndex, 32);
      retIndex += 32;
    }

    return retBytes;
  }

  public static byte[] encodeDynamicBytes(String value) {
    byte[] data = Hex.decode(value);
    List<DataWord> ret = new ArrayList<>();
    ret.add(new DataWord(data.length));

    int readInx = 0;
    int len = data.length;
    while (readInx < data.length) {
      byte[] wordData = new byte[32];
      int readLen = len - readInx >= 32 ? 32 : (len - readInx);
      System.arraycopy(data, readInx, wordData, 0, readLen);
      DataWord word = new DataWord(wordData);
      ret.add(word);
      readInx += 32;
    }

    byte[] retBytes = new byte[ret.size() * 32];
    int retIndex = 0;

    for (DataWord w : ret) {
      System.arraycopy(w.getData(), 0, retBytes, retIndex, 32);
      retIndex += 32;
    }

    return retBytes;
  }

  public static byte[] pack(List<Coder> codes, List<Object> values) throws EncodingException {

    int staticSize = 0;
    int dynamicSize = 0;

    List<byte[]> encodedList = new ArrayList<>();

    for (int idx = 0; idx < codes.size(); idx++) {
      Coder coder = codes.get(idx);
      Object parameter = values.get(idx);
      String value;
      if (parameter instanceof List) {
        StringBuilder sb = new StringBuilder();
        for (Object item : (List) parameter) {
          if (sb.length() != 0) {
            sb.append(",");
          }
          sb.append("\"").append(item).append("\"");
        }
        value = "[" + sb.toString() + "]";
      } else {
        value = parameter.toString();
      }
      byte[] encoded = coder.encode(value);
      encodedList.add(encoded);

      if (coder.dynamic) {
        staticSize += 32;
        dynamicSize += encoded.length;
      } else {
        staticSize += encoded.length;
      }
    }

    int offset = 0;
    int dynamicOffset = staticSize;

    byte[] data = new byte[staticSize + dynamicSize];

    for (int idx = 0; idx < codes.size(); idx++) {
      Coder coder = codes.get(idx);

      if (coder.dynamic) {
        System.arraycopy(new DataWord(dynamicOffset).getData(), 0, data,
            offset, 32);
        offset += 32;

        System.arraycopy(encodedList.get(idx), 0, data, dynamicOffset,
            encodedList.get(idx).length);
        dynamicOffset += encodedList.get(idx).length;
      } else {
        System
            .arraycopy(encodedList.get(idx), 0, data, offset, encodedList.get(idx).length);
        offset += encodedList.get(idx).length;
      }
    }

    return data;
  }


  public static byte[] parseMethod(String methodSign) throws EncodingException {
    return parseMethod(methodSign, "", false);
  }

  public static byte[] parseMethod(String methodSign, String params) throws EncodingException {
    return parseMethod(methodSign, params, false);
  }

  public static byte[] parseMethod(String methodSign, List<Object> inputList)
      throws EncodingException {
    return parseMethod(methodSign, inputList, false);
  }

  public static byte[] parseMethod(String methodSign, List<Object> parameters, boolean isHex)
      throws EncodingException {
    if (parameters == null || parameters.isEmpty()) {
      return parseMethod(methodSign, "", isHex);
    } else {
      Object[] inputArr = new Object[parameters.size()];
      int i = 0;
      for (Object parameter : parameters) {
        if (parameter instanceof List) {
          StringBuilder sb = new StringBuilder();
          for (Object item : (List) parameter) {
            if (sb.length() != 0) {
              sb.append(",");
            }
            sb.append("\"").append(item).append("\"");
          }
          inputArr[i++] = "[" + sb.toString() + "]";
        } else {
          inputArr[i++] =
              (parameter instanceof String) ? ("\"" + parameter + "\"") : parameter;
        }
      }
      return parseMethod(methodSign, StringUtils.join(inputArr, ','), isHex);
    }
  }

  public static byte[] parseMethod(String methodSign, String input, boolean isHex)
      throws EncodingException {
    byte[] selector = new byte[4];
    System.arraycopy(Hash.sha3(methodSign.getBytes()), 0, selector, 0, 4);
    if (StringUtils.isEmpty(input)) {
      return selector;
    }
    if (isHex) {
      return Hex.decode(Hex.toHexString(selector) + input);
    }
    byte[] encodedParms = encodeInput(methodSign, input);

    return Hex.decode(Hex.toHexString(selector) + Hex.toHexString(encodedParms));
  }

  public static byte[] encodeInput(String methodSign, String input) throws EncodingException {
    ObjectMapper mapper = new ObjectMapper();
    input = "[" + input + "]";
    List<Object> items = null;
    try {
      items = mapper.readValue(input, List.class);
    } catch (IOException e) {
      e.printStackTrace();
    }

    List<Coder> coders = new ArrayList<>();
    for (String s : getTypes(methodSign)) {
      Coder c = getParamCoder(s);
      coders.add(c);
    }

    return pack(coders, items);
  }

  public static void test() {
    String arrayMethod3 = "test(uint256[],address[])";

    //  String str = "1,[\\\"TNNqZuYhMfQvooC4kJwTsMJEQVU3vWGa5u\\\",\"TNNqZuYhMfQvooC4kJwTsMJEQVU3vWGa5u\"]";

    List<Object> l = new ArrayList<>();

    l.add(Arrays.asList(1, 2, 3));
    List<String> addresses = new ArrayList<>();
    addresses.add("TNNqZuYhMfQvooC4kJwTsMJEQVU3vWGa5u");
    ;
    addresses.add("TNNqZuYhMfQvooC4kJwTsMJEQVU3vWGa5u");
    ;
    l.add(Arrays
        .asList("TNNqZuYhMfQvooC4kJwTsMJEQVU3vWGa5u", "TNNqZuYhMfQvooC4kJwTsMJEQVU3vWGa5u"));
    parseMethod(arrayMethod3, l);

//    System.out.println(str);
    try {
      System.out.println("token:" + Hex.toHexString(parseMethod(arrayMethod3, l)));
    } catch (EncodingException e) {
      e.printStackTrace();
    }

  }

  public static int WORD_LENGTH = 32;

  public static List<String> unpackAddressArray(byte[] data) {
    if (data.length % WORD_LENGTH != 0 || data.length < 3) {
      throw new IllegalArgumentException("Illegal array data length:" + data.length);
    }
//    int offset = DataWord.getDataWord(data, 0).intValue();
    int length = DataWord.getDataWord(data, 1).intValue();
    ArrayList<String> addressList = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      DataWord dataWord = DataWord.getDataWord(data, 2 + i);
      if (!dataWord.isZero()) {
        byte[] noprefix = ByteUtil.stripLeadingZeroes(dataWord.getData());
        addressList.add(WalletUtil.encode58CheckWithoutPrefix(noprefix));
      }
    }
    return addressList;
  }

  public static String unpackAddress(byte[] data) {
    if (data.length % WORD_LENGTH != 0 || data.length < 3) {
      throw new IllegalArgumentException("Illegal array data length:" + data.length);
    }
    DataWord dataWord = DataWord.getDataWord(data, 0);
    byte[] noprefix = ByteUtil.stripLeadingZeroes(dataWord.getData());
    return WalletUtil.encode58CheckWithoutPrefix(noprefix);
  }


  public static List<String> unpackOracleSigns(byte[] data) {
    if (data.length % WORD_LENGTH != 0 || data.length / WORD_LENGTH < 3) {
      return Lists.newArrayList();
    }
    ArrayList<Integer> indexList = new ArrayList<>();

    int des = 1;
    int length = DataWord.getDataWord(data, des).intValue();
    length += des++;
    do {
      indexList.add(DataWord.getDataWord(data, des).intValue());
    } while (des++ < length);
    des++;
    ArrayList<String> signList = new ArrayList<>();
    indexList.forEach(index -> {
      index += WORD_LENGTH * 2;
      int valueLength = DataWord.getDataWord(data, index / WORD_LENGTH).intValue();
      byte[] range = Arrays
          .copyOfRange(data, index + WORD_LENGTH, index + WORD_LENGTH + valueLength);
      signList.add(ByteArray.toHexString(range));
    });
    return signList;
  }

  public static SignListParam unpackSignListParam(byte[] data) {
    if (data.length % WORD_LENGTH != 0 || data.length / WORD_LENGTH < 3) {
      logger.info("con not unpack data to SignListParam .data is {}", ByteArray.toHexString(data));
      return new SignListParam(Lists.newArrayList(), Lists.newArrayList());
    }
    try {
      ArrayList<String> signList = getSignList(data, 0);
      ArrayList<String> addressList = getAddressList(data, 1);
      return new SignListParam(signList, addressList);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      logger.info("con not unpack data to SignListParam .data is {}", ByteArray.toHexString(data));
    }
    return new SignListParam(Lists.newArrayList(), Lists.newArrayList());
  }

  private static ArrayList<String> getAddressList(byte[] data, int desIndex) {
    int des = DataWord.getDataWord(data, desIndex).intValue() / WORD_LENGTH;
    int length = DataWord.getDataWord(data, des).intValue();
    length += des++;
    ArrayList<String> addressList = new ArrayList<>();
    do {
      byte[] address = DataWord.getDataWord(data, des).getLast20Bytes();
      addressList.add(WalletUtil.encode58CheckWithoutPrefix(address));
    } while (des++ < length);
    return addressList;
  }

  private static ArrayList<String> getSignList(byte[] data, int desIndex) {
    ArrayList<Integer> indexList = new ArrayList<>();
    ArrayList<String> signList = new ArrayList<>();
    int des = DataWord.getDataWord(data, desIndex).intValue() / WORD_LENGTH;
    int length = DataWord.getDataWord(data, des).intValue();

    length += des++;
    do {
      indexList.add(DataWord.getDataWord(data, des).intValue());
    } while (des++ < length);
    des++;
    indexList.forEach(index -> {
      index += WORD_LENGTH * 3;
      int valueLength = DataWord.getDataWord(data, index / WORD_LENGTH).intValue();
      byte[] range = Arrays
          .copyOfRange(data, index + WORD_LENGTH, index + WORD_LENGTH + valueLength);
      signList.add(ByteArray.toHexString(range));

    });
    return signList;
  }

  public static boolean unpackStatus(byte[] data) {
    return !(new DataWord(data)).isZero();
  }

  public static long unpackUint(byte[] data) {
    return new DataWord(data).longValue();
  }

  public static String unpackString(byte[] data) {
    if (data.length < 2 * WORD_LENGTH || data.length % WORD_LENGTH != 0) {
      return "";
    }

    int index = DataWord.getDataWord(data, 0).intValue();
    int valueLength = DataWord.getDataWord(data, index / WORD_LENGTH).intValue();
    if (valueLength > 0) {
      byte[] range = Arrays
          .copyOfRange(data, index + WORD_LENGTH, index + WORD_LENGTH + valueLength);
      return ByteArray.toStr(range);
    }
    return "";
  }

  public static void main(String[] args) throws EncodingException {

    Object[] arr = {1, 'a', "b", 3};
    System.out.println(StringUtils.join(arr, ","));
    //test();
    //test2();
    //test6();
    //test7();
    test9();
//    String method = "test(address,string,int)";
//    String method = "test(string,int2,string)";
//    String params = "asdf,3123,adf";
//
//    String arrayMethod1 = "test(uint,uint256[3])";
//    String arrayMethod2 = "test(uint,uint256[])";
//    String arrayMethod3 = "test(uint,address[])";
//    String byteMethod1 = "test(bytes32,bytes11)";
//    String tokenMethod = "test(trcToken,uint256)";
//    String tokenParams = "\"nmb\",111";
//
//    System.out.println("token:" + parseMethod(tokenMethod, tokenParams));
//
//
    //String method1 = "test(uint256,string,string,uint256[])";
    //String expected1 = "db103cf30000000000000000000000000000000000000000000000000000000000000005000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000c0000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000014200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000143000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000003";
//    String method2 = "test(uint256,string,string,uint256[3])";
//    String expected2 = "000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000003";
//    String listString = "1 ,\"B\",\"C\", [1, 2, 3]";
//
////    ["Adsfsdf","dsfsdf"]
//    try {
//      System.out.println(parseMethod(method1, listString));
//      System.out.println(parseMethod(method2, listString));
//
//      String bytesValue1 = "\"0112313\",112313";
//      String bytesValue2 = "123123123";
//

//      System.out.println(parseMethod(byteMethod1, bytesValue1));
//    } catch (EncodingException e) {
//      e.printStackTrace();
//    }
//
//
////    System.out.println(parseMethod(byteMethod1, bytesValue2));
//
////    String method3 = "voteForSingleWitness(address,uint256)";
////    String method3 = "voteForSingleWitness(address)";
////    String params3 = "\"TNNqZuYhMfQvooC4kJwTsMJEQVU3vWGa5u\"";
////
////    System.out.println(parseMethod(method3, params3));
  }

  public static void test2() {
    String data = "00000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000c00000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000014000000000000000000000000000000000000000000000000000000000000000051234567890000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000622222222901100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000007033333338902220000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000074444444490333300000000000000000000000000000000000000000000000000";
    List<String> strings = AbiUtil.unpackOracleSigns(ByteArray.fromHexString(data));
    strings.forEach(s -> System.out.println(s));
  }

  public static void test9() {
    String data = "000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000002600000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000e000000000000000000000000000000000000000000000000000000000000001c000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004323334320000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ab3536313334323166677364666773646667736466677364666773646667647366676473666766647366677364666773646667647366677364666772657772743332343533343274726577727472657772747734353234333577657274726577727435333234356577727477726572743233343533347472657772743332343533343231333432333435333234353332343533323435333234353233343533323435323334353233343334350000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008383634353433353400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004352452345324523452345234518567500000000000000000000000000000000000000000000000000001845345346750000000000000000000000000000000000000000000000019734534534534235";
    SignListParam signListParam = AbiUtil.unpackSignListParam(ByteArray.fromHexString(data));
    signListParam.getOracleSigns().forEach(s -> System.out.println(s));
    signListParam.getOracleAddresses().forEach(s -> System.out.println(s));
  }

  public static void test7() {
    String data = "0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000f6161616161616161616161616161610000000000000000000000000000000000";
    String strings = AbiUtil.unpackString(ByteArray.fromHexString(data));
    System.out.println(strings);
  }

  public static void test6() {
    String data = "00000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000415181fed1245777a9906445441708b3805470d9daee1168ada2d3936d9b228c55165b40f63a5f7c372c13aad9b1561acce3e8e2bc72480f5fe04bbbbccd6ec3320000000000000000000000000000000000000000000000000000000000000000";
    List<String> strings = AbiUtil.unpackOracleSigns(ByteArray.fromHexString(data));
    strings.forEach(s -> System.out.println(s));
  }

  public static void test3() {
    String arrayMethod3 = "test(bytes[])";
    List<Object> l = new ArrayList<>();
    //  String str = "1,[\\\"TNNqZuYhMfQvooC4kJwTsMJEQVU3vWGa5u\\\",\"TNNqZuYhMfQvooC4kJwTsMJEQVU3vWGa5u\"]";
    List<String> signs = new ArrayList<>();
    signs.add("111112222222");
    signs.add("222222333333");
    signs.addAll(Arrays
        .asList("333334444444", "44444445555555"));
    l.add(signs);
    parseMethod(arrayMethod3, l);

//    System.out.println(str);
    try {
      System.out.println("token:" + Hex.toHexString(parseMethod(arrayMethod3, l)));
    } catch (EncodingException e) {
      e.printStackTrace();
    }
  }
}
