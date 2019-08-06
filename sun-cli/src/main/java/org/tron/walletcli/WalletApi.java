package org.tron.walletcli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.AddressUtil;
import org.tron.core.exception.CipherException;
import org.tron.walletcli.keystore.CheckStrength;
import org.tron.walletcli.keystore.Credentials;
import org.tron.walletcli.keystore.Wallet;
import org.tron.walletcli.keystore.WalletFile;
import org.tron.walletcli.keystore.WalletUtils;
import org.tron.walletcli.utils.Utils;

public class WalletApi {

  private static final Logger logger = LoggerFactory.getLogger("WalletApi");
  private static final String FilePath = "Wallet";

  private List<WalletFile> walletFile = new ArrayList<>();
  private boolean loginState = false;
  private byte[] address;

  public static boolean passwordValid(char[] password) {
    if (ArrayUtils.isEmpty(password)) {
      throw new IllegalArgumentException("password is empty");
    }
    if (password.length < 6) {
      logger.warn("Warning: Password is too short !!");
      return false;
    }
    //Other rule;
    int level = CheckStrength.checkPasswordStrength(password);
    if (level <= 4) {
      System.out.println("Your password is too weak!");
      System.out.println("The password should be at least 8 characters.");
      System.out.println("The password should contains uppercase, lowercase, numeric and other.");
      System.out.println(
          "The password should not contain more than 3 duplicate numbers or letters; For example: 1111.");
      System.out.println(
          "The password should not contain more than 3 consecutive Numbers or letters; For example: 1234.");
      System.out.println("The password should not contain weak password combination; For example:");
      System.out.println("ababab, abcabc, password, passw0rd, p@ssw0rd, admin1234, etc.");
      return false;
    }
    return true;
  }


  public static WalletFile CreateWalletFile(byte[] password) throws CipherException {
    ECKey ecKey = new ECKey(Utils.getRandom());
    WalletFile walletFile = Wallet.createStandard(password, ecKey);
    return walletFile;
  }

  //  Create Wallet with a pritKey
  public static WalletFile CreateWalletFile(byte[] password, byte[] priKey) throws CipherException {
    ECKey ecKey = ECKey.fromPrivate(priKey);
    WalletFile walletFile = Wallet.createStandard(password, ecKey);
    return walletFile;
  }

  public boolean isLoginState() {
    return loginState;
  }

  public void logout() {
    loginState = false;
    walletFile.clear();
    this.walletFile = null;
  }

  public void setLogin() {
    loginState = true;
  }

  public boolean checkPassword(byte[] passwd) throws CipherException {
    return Wallet.validPassword(passwd, this.walletFile.get(0));
  }


  public static String store2Keystore(WalletFile walletFile) throws IOException {
    if (walletFile == null) {
      logger.warn("Warning: Store wallet failed, walletFile is null !!");
      return null;
    }
    File file = new File(FilePath);
    if (!file.exists()) {
      if (!file.mkdir()) {
        throw new IOException("Make directory failed!");
      }
    } else {
      if (!file.isDirectory()) {
        if (file.delete()) {
          if (!file.mkdir()) {
            throw new IOException("Make directory failed!");
          }
        } else {
          throw new IOException("File exists and can not be deleted!");
        }
      }
    }
    return WalletUtils.generateWalletFile(walletFile, file);
  }

  public static File selectWalletFile() {
    File file = new File(FilePath);
    if (!file.exists() || !file.isDirectory()) {
      return null;
    }

    File[] wallets = file.listFiles();
    if (ArrayUtils.isEmpty(wallets)) {
      return null;
    }

    File wallet;
    for (int i = 0; i < wallets.length; i++) {
      System.out.println("The " + (i + 1) + "th keystore file name is " + wallets[i].getName());
    }
    System.out.println(
        "Please choose between 1 and " + wallets.length + " . Enter n or N to cancelled");
    Scanner in = new Scanner(System.in);
    while (true) {
      String input = in.nextLine().trim();
      String num = input.split("\\s+")[0];
      if ("n".equalsIgnoreCase(num)) {
        return null;
      }
      int n;
      try {
        n = new Integer(num);
      } catch (NumberFormatException e) {
        System.out.println("Invaild number of " + num);
        System.out.println("Please choose again between 1 and " + wallets.length
            + " . Enter n or N to cancelled");
        continue;
      }
      if (n < 1 || n > wallets.length) {
        System.out.println("Please choose again between 1 and " + wallets.length
            + " . Enter n or N to cancelled");
        continue;
      }
      wallet = wallets[n - 1];
      break;
    }

    return wallet;
  }

  public static boolean changeKeystorePassword(byte[] oldPassword, byte[] newPassowrd)
      throws IOException, CipherException {
    File wallet = selectWalletFile();
    if (wallet == null) {
      throw new IOException(
          "No keystore file found, please use registerwallet or importwallet first!");
    }
    Credentials credentials = WalletUtils.loadCredentials(oldPassword, wallet);
    WalletUtils.updateWalletFile(newPassowrd, credentials.getEcKeyPair(), wallet, true);
    return true;
  }


  private static WalletFile loadWalletFile() throws IOException {
    File wallet = selectWalletFile();
    if (wallet == null) {
      return null;
    }
    return WalletUtils.loadWalletFile(wallet);
  }

  public WalletApi(WalletFile walletFile) {
    if (this.walletFile.isEmpty()) {
      this.walletFile.add(walletFile);
    } else {
      this.walletFile.set(0, walletFile);
    }
    this.address = AddressUtil.decodeFromBase58Check(walletFile.getAddress());
  }

  /**
   * load a Wallet from keystore
   */
  public static WalletApi loadWalletFromKeystore() throws IOException {
    WalletFile walletFile = loadWalletFile();
    if (Objects.isNull(walletFile)) {
      return null;
    }
    WalletApi walletApi = new WalletApi(walletFile);
    return walletApi;
  }

  public WalletFile getCurrentWalletFile() {
    return this.walletFile.get(0);
  }

  public byte[] getPrivateBytes(byte[] password) throws CipherException, IOException {
    WalletFile walletFile = loadWalletFile();
    return Wallet.decrypt2PrivateBytes(password, walletFile);
  }
}
