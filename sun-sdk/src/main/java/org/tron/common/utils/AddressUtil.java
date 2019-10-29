package org.tron.common.utils;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.crypto.Sha256Hash;
import org.tron.core.config.Parameter.CommonConstant;
import org.tron.sunserver.ServerApi;


@Slf4j
public class AddressUtil {

  public static boolean addressValid(byte[] address) {
    if (ArrayUtils.isEmpty(address)) {
      logger.warn("Warning: Address is empty !!");
      return false;
    }
    if (address.length != CommonConstant.ADDRESS_SIZE) {
      logger.warn(
          "Warning: Address length need " + CommonConstant.ADDRESS_SIZE + " but " + address.length
              + " !!");
      return false;
    }
    byte preFixbyte = address[0];
    if (preFixbyte != ServerApi.getAddressPreFixByte()) {
      logger
          .warn("Warning: Address need prefix with " + ServerApi.getAddressPreFixByte() + " but "
              + preFixbyte + " !!");
      return false;
    }
    //Other rule;
    return true;
  }

  public static boolean addressValid(String check58Address) {
    return addressValid(decodeFromBase58Check(check58Address));
  }

  public static String toTron58Address(ByteString input){
    return toTron58Address(input.toByteArray());
  }

  public static String toTron58Address(byte[] input) {
    byte[] bytes = new byte[21];
    bytes[0] = ServerApi.getAddressPreFixByte();
    if (input.length >= 20){
      System.arraycopy(input, input.length - 20, bytes, 1, 20);
    }else{
      System.arraycopy(input, 0, bytes, 21 - input.length, input.length);
    }
    return encode58Check(bytes);
  }

  public static String encode58Check(ByteString input) {
    return encode58Check(input.toByteArray());
  }

  public static String encode58Check(byte[] input) {
    byte[] hash0 = Sha256Hash.hash(input);
    byte[] hash1 = Sha256Hash.hash(hash0);
    byte[] inputCheck = new byte[input.length + 4];
    System.arraycopy(input, 0, inputCheck, 0, input.length);
    System.arraycopy(hash1, 0, inputCheck, input.length, 4);
    return Base58.encode(inputCheck);
  }

  public static byte[] decode58Check(String input) {
    byte[] decodeCheck = Base58.decode(input);
    if (decodeCheck.length <= 4) {
      return new byte[0];
    }
    byte[] decodeData = new byte[decodeCheck.length - 4];
    System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
    byte[] hash0 = Sha256Hash.hash(decodeData);
    byte[] hash1 = Sha256Hash.hash(hash0);
    if (hash1[0] == decodeCheck[decodeData.length] &&
        hash1[1] == decodeCheck[decodeData.length + 1] &&
        hash1[2] == decodeCheck[decodeData.length + 2] &&
        hash1[3] == decodeCheck[decodeData.length + 3]) {
      return decodeData;
    }
    return new byte[0];
  }

  public static byte[] decodeFromBase58Check(String addressBase58) {
    if (StringUtils.isEmpty(addressBase58)) {
      logger.warn("Warning: Address is empty !!");
      return new byte[0];
    }
    byte[] address = decode58Check(addressBase58);
    if (!addressValid(address)) {
      return new byte[0];
    }
    return address;
  }

  public static void main(String[] args){
    System.out.println(toTron58Address(ByteArray.fromHexString("fff11001")));
    System.out.println(toTron58Address(ByteArray.fromHexString("4100000000000000000000000000000000fff11001")));
  }
}
