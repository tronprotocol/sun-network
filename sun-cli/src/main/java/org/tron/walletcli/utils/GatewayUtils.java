package org.tron.walletcli.utils;

import static org.tron.common.utils.DataWord.WORD_LENGTH;

import java.util.Arrays;
import lombok.Getter;
import org.tron.common.crypto.Hash;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.DataWord;

public class GatewayUtils {

  static class WithdrawMsg {

    @Getter
    private DataWord fromAddress;
    @Getter
    private DataWord contractAddress;
    @Getter
    private DataWord tokenId;
    @Getter
    private DataWord value;
    @Getter
    private int type;
    @Getter
    private int status;

    public WithdrawMsg(DataWord fromAddress, DataWord contractAddress,
        DataWord tokenId, DataWord value, int type, int status) {
      this.fromAddress = fromAddress;
      this.contractAddress = contractAddress;
      this.tokenId = tokenId;
      this.value = value;
      this.type = type;
      this.status = status;
    }
  }

  public static boolean unpackBoolean(byte[] data) {
    if (data.length % WORD_LENGTH != 0 || data.length < 3) {
      throw new IllegalArgumentException("Illegal array data length:" + data.length);
    }
    DataWord dataWord = DataWord.getDataWord(data, 0);
    return !dataWord.isZero();
  }

  public static WithdrawMsg unpackWithdrawMsg(byte[] data) {
    if (data.length % WORD_LENGTH != 0 || data.length < WORD_LENGTH * 6) {
      throw new IllegalArgumentException("Illegal array data length:" + data.length);
    }
    DataWord fromDataWord = DataWord.getDataWord(data, 0);
    DataWord contractDataWord = DataWord.getDataWord(data, 1);
    DataWord tokenIdDataWord = DataWord.getDataWord(data, 2);
    DataWord valueDataWord = DataWord.getDataWord(data, 3);
    DataWord typeDataWord = DataWord.getDataWord(data, 4);
    DataWord statusDataWord = DataWord.getDataWord(data, 5);
    return new WithdrawMsg(fromDataWord, contractDataWord, tokenIdDataWord, valueDataWord,
        typeDataWord.intValue(), statusDataWord.intValue());
  }

  public static String getWithdrawMsgHash(byte[] data, long nonce) {
    WithdrawMsg withdrawMsg = unpackWithdrawMsg(data);
    switch (withdrawMsg.type) {
      case 0:
        return getWithdrawTRXDataHash(withdrawMsg, nonce);
      case 1:
        return getWithdrawTRC10DataHash(withdrawMsg, nonce);
      case 2:
      case 3:
        return getWithdrawTRCTokenDataHash(withdrawMsg, nonce);
    }
    return null;
  }

  public static String getWithdrawTRC10DataHash(WithdrawMsg withdrawMsg, long nonce) {
    byte[] fromBytes = withdrawMsg.getFromAddress().getLast20Bytes();
    byte[] tokenIdBytes = withdrawMsg.getTokenId().getData();
    byte[] valueBytes = withdrawMsg.getValue().getData();
    byte[] nonceBytes = new DataWord(ByteArray.fromLong(nonce)).getData();
    byte[] data = ByteUtil.merge(fromBytes, tokenIdBytes, valueBytes, nonceBytes);
    return ByteArray.toHexString(Hash.sha3(data));
  }

  public static String getWithdrawTRCTokenDataHash(WithdrawMsg withdrawMsg, long nonce) {
    byte[] fromBytes = withdrawMsg.getFromAddress().getLast20Bytes();
    byte[] mainChainAddressBytes = withdrawMsg.getContractAddress().getLast20Bytes();
    byte[] valueBytes = withdrawMsg.getValue().getData();
    byte[] nonceBytes = new DataWord(ByteArray.fromLong(nonce)).getData();
    byte[] data = ByteUtil.merge(fromBytes, mainChainAddressBytes, valueBytes, nonceBytes);
    return ByteArray.toHexString(Hash.sha3(data));
  }

  public static String getWithdrawTRXDataHash(WithdrawMsg withdrawMsg, long nonce) {
    byte[] fromBytes = withdrawMsg.getFromAddress().getLast20Bytes();
    byte[] valueBytes = withdrawMsg.getValue().getData();
    byte[] nonceBytes = new DataWord(ByteArray.fromLong(nonce)).getData();
    byte[] data = ByteUtil.merge(fromBytes, valueBytes, nonceBytes);
    return ByteArray.toHexString(Hash.sha3(data));
  }
}
