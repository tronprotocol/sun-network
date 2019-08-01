package org.tron.walletcli;

import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import org.tron.api.GrpcAPI.TransactionSignWeight;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.ECKey.ECDSASignature;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.protos.Protocol.Transaction;
import org.tron.sunserver.IMultiTransactionSign;
import org.tron.walletcli.keystore.StringUtils;
import org.tron.walletcli.keystore.Wallet;
import org.tron.walletcli.keystore.WalletFile;
import org.tron.walletcli.utils.Utils;

public class MultiSignTransactionImpl implements IMultiTransactionSign {


  @Override
  public Transaction addTransactionSign(Transaction transaction,
      TransactionSignWeight weight, byte[] chainId) {

    try {

      System.out.println("Current signWeight is:");
      System.out.println(Utils.printTransactionSignWeight(weight));
      System.out.println(
          "Transaction hex string is  " + ByteArray.toHexString(transaction.toByteArray()));

      WalletApi wallet = WalletApi.loadWalletFromKeystore();
      if (Objects.isNull(wallet)) {
        System.out.println("User cancelled");
        return null;
      }
      System.out.println("Please input your password.");
      char[] password = new char[0];
      password = Utils.inputPassword(false);
      byte[] passwd = StringUtils.char2Byte(password);
      StringUtils.clear(password);
      wallet.checkPassword(passwd);
      if (Objects.isNull(wallet)) {
        System.out.println("Warning: Login failed, Please registerWallet or importWallet first !!");
        return null;
      }
      WalletFile walletFile = wallet.getCurrentWalletFile();
      ECKey myKey = Wallet.decrypt(passwd, walletFile);
      return signTransaction(transaction, myKey, chainId);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (CipherException e) {
      System.out.println(e.getMessage());
    }
    return null;
  }

  @Override
  public Transaction setPermissionId(Transaction transaction) {
    try {
      System.out.println(
          "Transaction hex string is  " + ByteArray.toHexString(transaction.toByteArray()));
      System.out.println(
          "Please confirm and input your permission id, if input y or Y means default 0, other non-numeric characters will cancell transaction.");
      transaction = Utils.setPermissionId(transaction);
    } catch (CancelException e) {
      System.out.println("User cancelled");
      return null;
    }
    return transaction;
  }

  private Transaction signTransaction(Transaction transaction, ECKey myKey, byte[] chainId) {
    Transaction.Builder transactionBuilderSigned = transaction.toBuilder();
    byte[] hash = Sha256Hash.hash(transaction.getRawData().toByteArray());

    byte[] newHash;
    if (Objects.isNull(chainId) || chainId.length <= 0) {
      newHash = hash;
    } else {
      byte[] hashWithChainId = Arrays.copyOf(hash, hash.length + chainId.length);
      System.arraycopy(chainId, 0, hashWithChainId, hash.length, chainId.length);
      newHash = Sha256Hash.hash(hashWithChainId);
    }
    ECDSASignature signature = myKey.sign(newHash);
    ByteString bsSign = ByteString.copyFrom(signature.toByteArray());
    transactionBuilderSigned.addSignature(bsSign);
    return transactionBuilderSigned.build();
  }
}
