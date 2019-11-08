package stest.tron.wallet.common.client;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.SymmEncoder;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.FileUtil;
import org.tron.common.utils.Sha256Hash;
import org.tron.common.utils.Utils;
import org.tron.protos.Protocol.Witness;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.Base58;

/*class AccountComparator implements Comparator {

  public int compare(Object o1, Object o2) {
    return Long.compare(((Account) o2).getBalance(), ((Account) o1).getBalance());
  }
}*/

class WitnessComparator implements Comparator {

  public int compare(Object o1, Object o2) {
    return Long.compare(((Witness) o2).getVoteCount(), ((Witness) o1).getVoteCount());
  }
}

public class WalletClient {


  private static final Logger logger = LoggerFactory.getLogger("WalletClient");
  private static final String FilePath = "Wallet";
  private static String dbPath;
  private static String txtPath;
  private static byte addressPreFixByte = CommonConstant.ADD_PRE_FIX_BYTE_MAINNET;
  private ECKey ecKey = null;
  private boolean loginState = false;

  //  static {
  //    new Timer().schedule(new TimerTask() {
  //      @Override
  //      public void run() {
  //        String fullnode = selectFullNode();
  //        if(!"".equals(fullnode)) {
  //          rpcCli = new GrpcClient(fullnode);
  //        }
  //      }
  //    }, 3 * 60 * 1000, 3 * 60 * 1000);
  //  }


  /**
   * Creates a new WalletClient with a random ECKey or no ECKey.
   */

  public WalletClient(boolean genEcKey) {
    if (genEcKey) {
      this.ecKey = new ECKey(Utils.getRandom());
    }
  }

  /**
   * constructor.
   */

  //  Create Wallet with a pritKey
  public WalletClient(String priKey) {
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    this.ecKey = temKey;
  }

  /**
   * Creates a Wallet with an existing ECKey.
   */

  public WalletClient(final ECKey ecKey) {
    this.ecKey = ecKey;
  }

  public static byte getAddressPreFixByte() {
    return addressPreFixByte;
  }

  public static void setAddressPreFixByte(byte addressPreFixByte) {
    WalletClient.addressPreFixByte = addressPreFixByte;
  }

  public static String getDbPath() {
    return dbPath;
  }

  public static String getTxtPath() {
    return txtPath;
  }

  /**
   * Get a Wallet from storage.
   */

  public static WalletClient getWalletByStorage(String password) {
    String priKeyEnced = loadPriKey();
    if (priKeyEnced == null) {
      return null;
    }
    //dec priKey
    byte[] priKeyAscEnced = priKeyEnced.getBytes();
    byte[] priKeyHexEnced = Hex.decode(priKeyAscEnced);
    byte[] aesKey = getEncKey(password);
    byte[] priKeyHexPlain = SymmEncoder.AES128EcbDec(priKeyHexEnced, aesKey);
    String priKeyPlain = Hex.toHexString(priKeyHexPlain);
    return new WalletClient(priKeyPlain);
  }

  private static String loadPassword() {
    char[] buf = new char[0x100];
    int len = FileUtil.readData(FilePath, buf);
    if (len != 226) {
      return null;
    }
    return String.valueOf(buf, 0, 32);
  }

  /**
   * constructor.
   */

  public static String loadPubKey() {
    char[] buf = new char[0x100];
    int len = FileUtil.readData(FilePath, buf);
    if (len != 226) {
      return null;
    }
    return String.valueOf(buf, 32, 130);
  }

  private static String loadPriKey() {
    char[] buf = new char[0x100];
    int len = FileUtil.readData(FilePath, buf);
    if (len != 226) {
      return null;
    }
    return String.valueOf(buf, 162, 64);
  }

  /**
   * Get a Wallet from storage.
   */

  public static WalletClient getWalletByStorageIgnorPrivKey() {
    try {
      String pubKey = loadPubKey(); //04 PubKey[128]
      if (StringUtils.isEmpty(pubKey)) {
        return null;
      }
      byte[] pubKeyAsc = pubKey.getBytes();
      byte[] pubKeyHex = Hex.decode(pubKeyAsc);
      ECKey eccKey = ECKey.fromPublicOnly(pubKeyHex);
      return new WalletClient(eccKey);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * constructor.
   */

  public static String getAddressByStorage() {
    try {
      String pubKey = loadPubKey(); //04 PubKey[128]
      if (StringUtils.isEmpty(pubKey)) {
        return null;
      }
      byte[] pubKeyAsc = pubKey.getBytes();
      byte[] pubKeyHex = Hex.decode(pubKeyAsc);
      ECKey eccKey = ECKey.fromPublicOnly(pubKeyHex);
      return ByteArray.toHexString(eccKey.getAddress());
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * constructor.
   */

  public static byte[] getPassWord(String password) {
    if (!passwordValid(password)) {
      return null;
    }
    byte[] pwd;
    pwd = Sha256Hash.hash(password.getBytes());
    pwd = Sha256Hash.hash(pwd);
    pwd = Arrays.copyOfRange(pwd, 0, 16);
    return pwd;
  }

  /**
   * constructor.
   */

  public static byte[] getEncKey(String password) {
    if (!passwordValid(password)) {
      return null;
    }
    byte[] encKey;
    encKey = Sha256Hash.hash(password.getBytes());
    encKey = Arrays.copyOfRange(encKey, 0, 16);
    return encKey;
  }

  /**
   * constructor.
   */

  public static boolean checkPassWord(String password) {
    byte[] pwd = getPassWord(password);
    if (pwd == null) {
      return false;
    }
    String pwdAsc = ByteArray.toHexString(pwd);
    String pwdInstore = loadPassword();
    return pwdAsc.equals(pwdInstore);
  }

  /**
   * constructor.
   */

  public static boolean passwordValid(String password) {
    if (StringUtils.isEmpty(password)) {
      logger.warn("Warning: Password is empty !!");
      return false;
    }
    if (password.length() < 6) {
      logger.warn("Warning: Password is too short !!");
      return false;
    }
    //Other rule;
    return true;
  }

  /**
   * constructor.
   */

  public static boolean addressValid(byte[] address) {
    if (address == null || address.length == 0) {
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
    if (preFixbyte != getAddressPreFixByte()) {
      logger.warn("Warning: Address need prefix with " + getAddressPreFixByte() + " but "
          + preFixbyte + " !!");
      return false;
    }
    //Other rule;
    return true;
  }

  /**
   * constructor.
   */

  public static String encode58Check(byte[] input) {
    byte[] hash0 = Sha256Hash.hash(input);
    byte[] hash1 = Sha256Hash.hash(hash0);
    byte[] inputCheck = new byte[input.length + 4];
    System.arraycopy(input, 0, inputCheck, 0, input.length);
    System.arraycopy(hash1, 0, inputCheck, input.length, 4);
    return Base58.encode(inputCheck);
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
    if (hash1[0] == decodeCheck[decodeData.length]
        && hash1[1] == decodeCheck[decodeData.length + 1]
        && hash1[2] == decodeCheck[decodeData.length + 2]
        && hash1[3] == decodeCheck[decodeData.length + 3]) {
      return decodeData;
    }
    return null;
  }

  /**
   * constructor.
   */

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

  public boolean login(String password) {
    loginState = checkPassWord(password);
    return loginState;
  }

  public boolean isLoginState() {
    return loginState;
  }

  public void logout() {
    loginState = false;
  }

  public ECKey getEcKey() {
    return ecKey;
  }

  public byte[] getAddress() {
    return ecKey.getAddress();
  }

}
