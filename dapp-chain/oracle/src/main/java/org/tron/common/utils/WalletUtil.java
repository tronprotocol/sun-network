package org.tron.common.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.tron.common.crypto.ECKey;
import org.tron.keystore.CipherException;
import org.tron.keystore.Credentials;
import org.tron.keystore.Wallet;
import org.tron.keystore.WalletFile;


@Slf4j
public class WalletUtil {

  public static byte[] decodeFromBase58Check(String addressBase58) {
    if (StringUtils.isEmpty(addressBase58)) {
      logger.warn("Warning: Address is empty !!");
      return null;
    }
    byte[] address = decode58Check(addressBase58);
    if (!addressValid(address)) {
      return null;
    }
    return address;
  }

  private static byte[] decode58Check(String input) {
    byte[] decodeCheck = Base58.decode(input);
    if (decodeCheck.length <= 4) {
      return null;
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
    return null;
  }

  public static boolean addressValid(byte[] address) {
    if (ArrayUtils.isEmpty(address)) {
      logger.warn("Warning: Address is empty !!");
      return false;
    }
    if (address.length != CommonConstant.ADDRESS_SIZE) {
      logger.warn(
          "Warning: Address length need " + CommonConstant.ADDRESS_SIZE + " but "
              + address.length
              + " !!");
      return false;
    }
    return true;
  }

  public static byte getAddressPreFixByte() {
    return CommonConstant.ADD_PRE_FIX_BYTE_MAINNET;
  }

  public static String encode58CheckForTron(byte[] input) {
    if (input.length <= 20) {
      return encode58CheckWithoutPrefix(input);
    } else {
      return encode58Check(input);
    }
  }

  public static String encode58Check(byte[] input) {
    byte[] hash0 = Sha256Hash.hash(input);
    byte[] hash1 = Sha256Hash.hash(hash0);
    byte[] inputCheck = new byte[input.length + 4];
    System.arraycopy(input, 0, inputCheck, 0, input.length);
    System.arraycopy(hash1, 0, inputCheck, input.length, 4);
    return Base58.encode(inputCheck);
  }

  public static String encode58CheckWithoutPrefix(byte[] input) {
    byte[] newInput = new byte[21];
    newInput[0] = CommonConstant.ADD_PRE_FIX_BYTE_MAINNET;
    System.arraycopy(input, 0, newInput, 1, input.length);
    return encode58Check(newInput);
  }

  public static void sleep(long interval) {
    try {
      Thread.sleep(interval);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static String generateFullNewWalletFile(String password, File destinationDirectory)
      throws NoSuchAlgorithmException, NoSuchProviderException,
      InvalidAlgorithmParameterException, CipherException, IOException {

    return generateNewWalletFile(password, destinationDirectory, true);
  }

  public static String generateLightNewWalletFile(String password, File destinationDirectory)
      throws NoSuchAlgorithmException, NoSuchProviderException,
      InvalidAlgorithmParameterException, CipherException, IOException {

    return generateNewWalletFile(password, destinationDirectory, false);
  }

  public static String generateNewWalletFile(
      String password, File destinationDirectory, boolean useFullScrypt)
      throws CipherException, IOException, InvalidAlgorithmParameterException,
      NoSuchAlgorithmException, NoSuchProviderException {

    ECKey ecKeyPair = new ECKey(SignUtils.getRandom());
    return generateWalletFile(password, ecKeyPair, destinationDirectory, useFullScrypt);
  }

  public static String generateWalletFile(
      String password, ECKey ecKeyPair, File destinationDirectory, boolean useFullScrypt)
      throws CipherException, IOException {

    WalletFile walletFile;
    if (useFullScrypt) {
      walletFile = Wallet.createStandard(password, ecKeyPair);
    } else {
      walletFile = Wallet.createLight(password, ecKeyPair);
    }

    String fileName = getWalletFileName(walletFile);
    File destination = new File(destinationDirectory, fileName);

    objectMapper.writeValue(destination, walletFile);

    return fileName;
  }

  public static void updateWalletFile(
      String password, ECKey ecKeyPair, File source, boolean useFullScrypt)
      throws CipherException, IOException {

    WalletFile walletFile = objectMapper.readValue(source, WalletFile.class);
    if (useFullScrypt) {
      walletFile = Wallet.createStandard(password, ecKeyPair);
    } else {
      walletFile = Wallet.createLight(password, ecKeyPair);
    }

    objectMapper.writeValue(source, walletFile);
  }

  public static Credentials loadCredentials(String password, File source)
      throws IOException, CipherException {
    WalletFile walletFile = objectMapper.readValue(source, WalletFile.class);
    return Credentials.create(Wallet.decrypt(password, walletFile));
  }

  private static String getWalletFileName(WalletFile walletFile) {
    DateTimeFormatter format = DateTimeFormatter.ofPattern(
        "'UTC--'yyyy-MM-dd'T'HH-mm-ss.nVV'--'");
    ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

    return now.format(format) + walletFile.getAddress() + ".json";
  }

  public static String getDefaultKeyDirectory() {
    return getDefaultKeyDirectory(System.getProperty("os.name"));
  }

  static String getDefaultKeyDirectory(String osName1) {
    String osName = osName1.toLowerCase();

    if (osName.startsWith("mac")) {
      return String.format(
          "%s%sLibrary%sEthereum", System.getProperty("user.home"), File.separator,
          File.separator);
    } else if (osName.startsWith("win")) {
      return String.format("%s%sEthereum", System.getenv("APPDATA"), File.separator);
    } else {
      return String.format("%s%s.ethereum", System.getProperty("user.home"), File.separator);
    }
  }

  public static String getTestnetKeyDirectory() {
    return String.format(
        "%s%stestnet%skeystore", getDefaultKeyDirectory(), File.separator, File.separator);
  }

  public static String getMainnetKeyDirectory() {
    return String.format("%s%skeystore", getDefaultKeyDirectory(), File.separator);
  }

  public static boolean passwordValid(String password) {
    if (StringUtils.isEmpty(password)) {
      return false;
    }
    if (password.length() < 6) {
      return false;
    }
    //Other rule;
    return true;
  }

  public static String inputPassword() {
    Scanner in = null;
    String password;
    Console cons = System.console();
    if (cons == null) {
      in = new Scanner(System.in);
    }
    while (true) {
      if (cons != null) {
        char[] pwd = cons.readPassword("password: ");
        password = String.valueOf(pwd);
      } else {
        String input = in.nextLine().trim();
        password = input.split("\\s+")[0];
      }
      if (passwordValid(password)) {
        return password;
      }
      System.out.println("Invalid password, please input again.");
    }
  }

  public static String inputPassword2Twice() {
    String password0;
    while (true) {
      System.out.println("Please input password.");
      password0 = inputPassword();
      System.out.println("Please input password again.");
      String password1 = inputPassword();
      if (password0.equals(password1)) {
        break;
      }
      System.out.println("The passwords do not match, please input again.");
    }
    return password0;
  }

  public static String bigIntegerStrAdd(String base, String nonce) {
    return new BigInteger(base).add(new BigInteger(nonce)).toString();
  }
}
