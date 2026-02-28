package org.tron.walletcli;

import com.beust.jcommander.JCommander;
import com.google.common.primitives.Longs;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI.AccountNetMessage;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.GrpcAPI.AddressPrKeyPairMessage;
import org.tron.api.GrpcAPI.AssetIssueList;
import org.tron.api.GrpcAPI.BlockExtention;
import org.tron.api.GrpcAPI.BlockListExtention;
import org.tron.api.GrpcAPI.DelegatedResourceList;
import org.tron.api.GrpcAPI.ExchangeList;
import org.tron.api.GrpcAPI.Node;
import org.tron.api.GrpcAPI.NodeList;
import org.tron.api.GrpcAPI.NumberMessage;
import org.tron.api.GrpcAPI.ProposalList;
import org.tron.api.GrpcAPI.SideChainProposalList;
import org.tron.api.GrpcAPI.TransactionApprovedList;
import org.tron.api.GrpcAPI.TransactionListExtention;
import org.tron.api.GrpcAPI.TransactionSignWeight;
import org.tron.api.GrpcAPI.WitnessList;
import org.tron.common.crypto.Hash;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.AddressUtil;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.Similarity;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.core.exception.EncodingException;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.ChainParameters;
import org.tron.protos.Protocol.DelegatedResourceAccountIndex;
import org.tron.protos.Protocol.Exchange;
import org.tron.protos.Protocol.Proposal;
import org.tron.protos.Protocol.SideChainParameters;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.sunapi.ErrorCodeEnum;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.response.TransactionResponse;
import org.tron.sunserver.ServerApi;
import org.tron.walletcli.keystore.StringUtils;
import org.tron.walletcli.utils.Utils;

public class Client {

  private static final Logger logger = LoggerFactory.getLogger("Client");
  private WalletApiWrapper walletApiWrapper = new WalletApiWrapper();

  private char[] inputPassword2Twice() throws IOException {
    char[] password0;
    while (true) {
      System.out.println("Please input password.");
      password0 = Utils.inputPassword(true);
      System.out.println("Please input password again.");
      char[] password1 = Utils.inputPassword(true);
      boolean flag = Arrays.equals(password0, password1);
      StringUtils.clear(password1);
      if (flag) {
        break;
      }
      System.out.println("The passwords do not match, please input again.");
    }
    return password0;
  }

  private byte[] inputPrivateKey() throws IOException {
    byte[] temp = new byte[128];
    byte[] result = null;
    System.out.println("Please input private key.");
    while (true) {
      int len = System.in.read(temp, 0, temp.length);
      if (len >= 64) {
        byte[] privateKey = Arrays.copyOfRange(temp, 0, 64);
        result = StringUtils.hexs2Bytes(privateKey);
        StringUtils.clear(privateKey);
        if (Utils.priKeyValid(result)) {
          break;
        }
      }
      StringUtils.clear(result);
      System.out.println("Invalid private key, please input again.");
    }
    StringUtils.clear(temp);
    return result;
  }

  private byte[] inputPrivateKey64() throws IOException {
    Decoder decoder = Base64.getDecoder();
    byte[] temp = new byte[128];
    byte[] result;
    System.out.println("Please input private key by base64.");
    while (true) {
      int len = System.in.read(temp, 0, temp.length);
      if (len >= 44) {
        byte[] priKey64 = Arrays.copyOfRange(temp, 0, 44);
        result = decoder.decode(priKey64);
        StringUtils.clear(priKey64);
        if (Utils.priKeyValid(result)) {
          break;
        }
      }
      System.out.println("Invalid base64 private key, please input again.");
    }
    StringUtils.clear(temp);
    return result;
  }

  private void registerWallet() throws CipherException, IOException {
    char[] password = inputPassword2Twice();
    String fileName = walletApiWrapper.registerWallet(password);
    StringUtils.clear(password);

    if (null == fileName) {
      logger.info("Register wallet failed !!");
      return;
    }
    logger.info("Register a wallet successful, keystore file name is " + fileName);
  }

  private void importWallet() throws CipherException, IOException {
    char[] password = inputPassword2Twice();
    byte[] priKey = inputPrivateKey();

    String fileName = walletApiWrapper.importWallet(password, priKey);
    StringUtils.clear(password);
    StringUtils.clear(priKey);

    if (null == fileName) {
      System.out.println("Import wallet failed !!");
      return;
    }
    System.out.println("Import a wallet successful, keystore file name is " + fileName);
  }

  private void importwalletByBase64() throws CipherException, IOException {
    char[] password = inputPassword2Twice();
    byte[] priKey = inputPrivateKey64();

    String fileName = walletApiWrapper.importWallet(password, priKey);
    StringUtils.clear(password);
    StringUtils.clear(priKey);

    if (null == fileName) {
      System.out.println("Import wallet failed !!");
      return;
    }
    System.out.println("Import a wallet successful, keystore file name is " + fileName);
  }

  private void changePassword() throws IOException, CipherException {
    System.out.println("Please input old password.");
    char[] oldPassword = Utils.inputPassword(false);
    System.out.println("Please input new password.");
    char[] newPassword = inputPassword2Twice();
    StringUtils.clear(oldPassword);
    StringUtils.clear(newPassword);
    if (walletApiWrapper.changePassword(oldPassword, newPassword)) {
      System.out.println("ChangePassword successful !!");
    } else {
      System.out.println("ChangePassword failed !!");
    }

  }

  private void login() throws IOException, CipherException {
    boolean result = walletApiWrapper.login();
    if (result) {
      System.out.println("Login successful !!!");
    } else {
      System.out.println("Login failed !!!");
    }
  }

  private void logout() {
    walletApiWrapper.logout();
    System.out.println("Logout successful !!!");
  }

  private void backupWallet() throws IOException, CipherException {
    byte[] priKey = walletApiWrapper.backupWallet();

    if (!ArrayUtils.isEmpty(priKey)) {
      System.out.println("BackupWallet successful !!");
      for (int i = 0; i < priKey.length; i++) {
        StringUtils.printOneByte(priKey[i]);
      }
      System.out.println();
    }
    StringUtils.clear(priKey);
  }

  private void backupWallet2Base64() throws IOException, CipherException {
    byte[] priKey = walletApiWrapper.backupWallet();

    if (!ArrayUtils.isEmpty(priKey)) {
      Encoder encoder = Base64.getEncoder();
      byte[] priKey64 = encoder.encode(priKey);
      StringUtils.clear(priKey);
      System.out.println("BackupWallet successful !!");
      for (int i = 0; i < priKey64.length; i++) {
        System.out.print((char) priKey64[i]);
      }
      System.out.println();
      StringUtils.clear(priKey64);
    }
  }

  private void getAddress() {
    String address = walletApiWrapper.getAddress();
    if (address != null) {
      logger.info("GetAddress successful !!");
      logger.info("address = " + address);
    }
  }

  private void getBalance() {
    SunNetworkResponse<Long> result = walletApiWrapper.getBalance();
    if (result != null && result.getCode().equals(ErrorCodeEnum.SUCCESS.getCode())) {
      long balance = result.getData();
      logger.info("Balance = " + balance);
    } else {
      logger.info("GetBalance failed !!!!");
    }
  }

  private void getAccount(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("GetAccount needs 1 parameter like the following: ");
      System.out.println("GetAccount Address ");
      return;
    }
    String address = parameters[0];
    byte[] addressBytes = AddressUtil.decodeFromBase58Check(address);
    if (addressBytes == null) {
      return;
    }

    SunNetworkResponse<Account> result = walletApiWrapper.getAccount(address);
    if (result != null && result.getCode().equals(ErrorCodeEnum.SUCCESS.getCode())) {
      logger.info("\n" + Utils.printAccount(result.getData()));
    } else {
      logger.info("GetAccount failed !!!!");
    }
  }

  private void getAccountById(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("GetAccountById needs 1 parameter like the following: ");
      System.out.println("GetAccountById accountId ");
      return;
    }
    String accountId = parameters[0];

    Account account = walletApiWrapper.queryAccountById(accountId);
    if (account == null) {
      logger.info("GetAccountById failed !!!!");
    } else {
      logger.info("\n" + Utils.printAccount(account));
    }
  }


  private void updateAccount(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("UpdateAccount needs 1 parameter like the following: ");
      System.out.println("UpdateAccount AccountName ");
      return;
    }

    String accountName = parameters[0];
    SunNetworkResponse<TransactionResponse> result = walletApiWrapper.updateAccount(accountName);
    if (checkResult(result)) {
      logger.info("Update Account successful !!!!");
    } else {
      logger.info("Update Account failed !!!!");
    }
  }

  private void setAccountId(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("SetAccountId needs 1 parameter like the following: ");
      System.out.println("SetAccountId AccountId ");
      return;
    }

    String accountId = parameters[0];
    SunNetworkResponse<TransactionResponse> result = walletApiWrapper.setAccountId(accountId);
    if (checkResult(result)) {
      logger.info("Set AccountId successful !!!!");
    } else {
      logger.info("Set AccountId failed !!!!");
    }
  }

  private void updateAsset(String[] parameters)
      throws IOException, CipherException, CancelException {
    if (parameters == null || parameters.length != 4) {
      System.out.println("UpdateAsset needs 4 parameters like the following: ");
      System.out.println("UpdateAsset newLimit newPublicLimit description url");
      return;
    }

    String newLimitString = parameters[0];
    String newPublicLimitString = parameters[1];
    String description = parameters[2];
    String url = parameters[3];

    SunNetworkResponse<TransactionResponse> result = walletApiWrapper
        .updateAsset(newLimitString, newPublicLimitString, description, url);
    if (checkResult(result)) {
      logger.info("Update Asset successful !!!!");
    } else {
      logger.info("Update Asset failed !!!!");
    }
  }

  private void getAssetIssueByAccount(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("GetAssetIssueByAccount needs 1 parameter like following: ");
      System.out.println("GetAssetIssueByAccount Address ");
      return;
    }
    String address = parameters[0];

    Optional<AssetIssueList> result = walletApiWrapper.getAssetIssueByAccount(address);
    if (result.isPresent()) {
      AssetIssueList assetIssueList = result.get();
      logger.info(Utils.printAssetIssueList(assetIssueList));
    } else {
      logger.info("GetAssetIssueByAccount " + " failed !!");
    }
  }

  private void getAccountNet(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("GetAccountNet needs 1 parameter like following: ");
      System.out.println("GetAccountNet Address ");
      return;
    }
    String address = parameters[0];

    AccountNetMessage result = walletApiWrapper.getAccountNet(address);
    if (result == null) {
      logger.info("GetAccountNet " + " failed !!");
    } else {
      logger.info("\n" + Utils.printAccountNet(result));
    }
  }

  private void getAccountResource(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("getAccountResource needs 1 parameter like following: ");
      System.out.println("getAccountResource Address ");
      return;
    }
    String address = parameters[0];

    AccountResourceMessage result = walletApiWrapper.getAccountResource(address);
    if (result == null) {
      logger.info("getAccountResource " + " failed !!");
    } else {
      logger.info("\n" + Utils.printAccountResourceMessage(result));
    }
  }

  // In 3.2 version, this function will return null if there are two or more asset with the same token name,
  // so please use getAssetIssueById or getAssetIssueListByName.
  // This function just remains for compatibility.
  private void getAssetIssueByName(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("GetAssetIssueByName needs 1 parameter like following: ");
      System.out.println("GetAssetIssueByName AssetName ");
      return;
    }
    String assetName = parameters[0];

    AssetIssueContract assetIssueContract = walletApiWrapper.getAssetIssueByName(assetName);
    if (assetIssueContract != null) {
      logger.info("\n" + Utils.printAssetIssue(assetIssueContract));
    } else {
      logger.info("getAssetIssueByName " + " failed !!");
    }
  }

  private void getAssetIssueListByName(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("getAssetIssueListByName needs 1 parameter like following: ");
      System.out.println("getAssetIssueListByName AssetName ");
      return;
    }
    String assetName = parameters[0];

    AssetIssueList result = walletApiWrapper.getAssetIssueListByName(assetName);
    if (result != null) {
      logger.info(Utils.printAssetIssueList(result));
    } else {
      logger.info("getAssetIssueListByName " + " failed !!");
    }
  }

  private void getAssetIssueById(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("getAssetIssueById needs 1 parameter like following: ");
      System.out.println("getAssetIssueById AssetId ");
      return;
    }
    String assetId = parameters[0];

    AssetIssueContract assetIssueContract = walletApiWrapper.getAssetIssueById(assetId);
    if (assetIssueContract != null) {
      logger.info("\n" + Utils.printAssetIssue(assetIssueContract));
    } else {
      logger.info("getAssetIssueById " + " failed !!");
    }
  }

  private void sendCoin(String[] parameters) {
    if (parameters == null || parameters.length != 2) {
      System.out.println("SendCoin needs 2 parameters like following: ");
      System.out.println("SendCoin ToAddress Amount");
      return;
    }

    String toAddress = parameters[0];
    String amountStr = parameters[1];
    long amount = new Long(amountStr);

    SunNetworkResponse<TransactionResponse> result = walletApiWrapper.sendCoin(toAddress, amount);
    if (checkResult(result)) {
      logger.info("transaction id is " + result.getData().trxId);
      logger.info("Send " + amount + " drop to " + toAddress + " successful !!\n "
          + "Please check the given transaction id to get the result on blockchain using getTransactionInfoById command");
    } else {
      logger.info("Send " + amount + " drop to " + toAddress + " failed !!");
    }
  }

  private void testTransaction(String[] parameters) {
    if (parameters == null || (parameters.length != 3 && parameters.length != 4)) {
      System.out.println("testTransaction needs 3 or 4 parameters using the following syntax: ");
      System.out.println("testTransaction ToAddress assertName times");
      System.out.println("testTransaction ToAddress assertName times interval");
      System.out.println("If needing transferAsset, assertName input null");
      return;
    }

    String toAddress = parameters[0];
    String assertName = parameters[1];
    String loopTime = parameters[2];
    int intervalInt = 0;//s
    if (parameters.length == 4) {
      String interval = parameters[3];
      intervalInt = Integer.parseInt(interval);//s
    }
    intervalInt *= 500; //ms
    long times = new Long(loopTime);

    for (int i = 1; i <= times; i++) {
      long amount = i;
      SunNetworkResponse<TransactionResponse> result = walletApiWrapper.sendCoin(toAddress, amount);
      if (checkResult(result)) {
        logger.info("Send " + amount + " drop to " + toAddress + " successful !!");
        if (intervalInt > 0) {
          try {
            Thread.sleep(intervalInt);
          } catch (Exception e) {
            e.printStackTrace();
            break;
          }
        }
      } else {
        logger.info("Send " + amount + " drop to " + toAddress + " failed !!");
        break;
      }

      if (!"null".equalsIgnoreCase(assertName)) {
        result = walletApiWrapper.transferAsset(toAddress, assertName, amount);
        if (checkResult(result)) {
          logger
              .info(
                  "transferAsset " + amount + assertName + " to " + toAddress + " successful !!");
          if (intervalInt > 0) {
            try {
              Thread.sleep(intervalInt);
            } catch (Exception e) {
              e.printStackTrace();
              break;
            }
          }
        } else {
          logger.info("transferAsset " + amount + assertName + " to " + toAddress + " failed !!");
          break;
        }
      }
    }
  }

  private void transferAsset(String[] parameters) {
    if (parameters == null || parameters.length != 3) {
      System.out.println("TransferAsset needs 3 parameters using the following syntax: ");
      System.out.println("TransferAsset ToAddress AssertName Amount");
      return;
    }

    String toAddress = parameters[0];
    String assertName = parameters[1];
    String amountStr = parameters[2];
    long amount = new Long(amountStr);

    SunNetworkResponse<TransactionResponse> result = walletApiWrapper
        .transferAsset(toAddress, assertName, amount);
    if (checkResult(result)) {
      logger.info("TransferAsset " + amount + " to " + toAddress + " successful !!");
    } else {
      logger.info("TransferAsset " + amount + " to " + toAddress + " failed !!");
    }
  }

  private void participateAssetIssue(String[] parameters) {
    if (parameters == null || parameters.length != 3) {
      System.out.println("ParticipateAssetIssue needs 3 parameters using the following syntax: ");
      System.out.println("ParticipateAssetIssue ToAddress AssetName Amount");
      return;
    }

    String toAddress = parameters[0];
    String assertName = parameters[1];
    String amountStr = parameters[2];
    long amount = Long.parseLong(amountStr);

    SunNetworkResponse<TransactionResponse> result = walletApiWrapper
        .participateAssetIssue(toAddress, assertName, amount);
    if (checkResult(result)) {
      logger.info("ParticipateAssetIssue " + assertName + " " + amount + " from " + toAddress
          + " successful !!");
    } else {
      logger.info("ParticipateAssetIssue " + assertName + " " + amount + " from " + toAddress
          + " failed !!");
    }
  }

  private void assetIssue(String[] parameters) {
    if (parameters == null || parameters.length < 11 || (parameters.length & 1) == 0) {
      System.out
          .println("Use the assetIssue command for features that you require with below syntax: ");
      System.out.println(
          "AssetIssue AssetName TotalSupply TrxNum AssetNum Precision "
              + "StartDate EndDate Description Url FreeNetLimitPerAccount PublicFreeNetLimit "
              + "FrozenAmount0 FrozenDays0 ... FrozenAmountN FrozenDaysN");
      System.out
          .println(
              "TrxNum and AssetNum represents the conversion ratio of the tron to the asset.");
      System.out
          .println("The StartDate and EndDate format should look like 2018-3-1 2018-3-21 .");
      return;
    }

    String name = parameters[0];
    String totalSupplyStr = parameters[1];
    String trxNumStr = parameters[2];
    String icoNumStr = parameters[3];
    String precisionStr = parameters[4];
    String startYyyyMmDd = parameters[5];
    String endYyyyMmDd = parameters[6];
    String description = parameters[7];
    String url = parameters[8];
    String freeNetLimitPerAccount = parameters[9];
    String publicFreeNetLimitString = parameters[10];
    HashMap<String, String> frozenSupply = new HashMap<>();
    for (int i = 11; i < parameters.length; i += 2) {
      String amount = parameters[i];
      String days = parameters[i + 1];
      frozenSupply.put(days, amount);
    }

    boolean result = walletApiWrapper
        .assetIssue(name, totalSupplyStr, trxNumStr, icoNumStr, precisionStr,
            startYyyyMmDd, endYyyyMmDd, description, url, freeNetLimitPerAccount,
            publicFreeNetLimitString, frozenSupply);
    if (result) {
      logger.info("AssetIssue " + name + " successful !!");
    } else {
      logger.info("AssetIssue " + name + " failed !!");
    }
  }

  private void createAccount(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("CreateAccount needs 1 parameter using the following syntax: ");
      System.out.println("CreateAccount Address");
      return;
    }

    String address = parameters[0];

    SunNetworkResponse<TransactionResponse> result = walletApiWrapper.createAccount(address);
    if (checkResult(result)) {
      logger.info("CreateAccount " + " successful !!");
    } else {
      logger.info("CreateAccount " + " failed !!");
    }
  }

  private void createWitness(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("CreateWitness needs 1 parameter using the following syntax: ");
      System.out.println("CreateWitness Url");
      return;
    }

    String url = parameters[0];

    SunNetworkResponse<TransactionResponse> result = walletApiWrapper.createWitness(url);
    if (checkResult(result)) {
      logger.info("CreateWitness " + " successful !!");
    } else {
      logger.info("CreateWitness " + " failed !!");
    }
  }

  private void updateWitness(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("updateWitness needs 1 parameter using the following syntax: ");
      System.out.println("updateWitness Url");
      return;
    }

    String url = parameters[0];

    SunNetworkResponse<TransactionResponse> result = walletApiWrapper.updateWitness(url);
    if (checkResult(result)) {
      logger.info("updateWitness " + " successful !!");
    } else {
      logger.info("updateWitness " + " failed !!");
    }
  }

  private boolean checkResult(SunNetworkResponse<TransactionResponse> result) {
    return result != null && result.getCode().equals(ErrorCodeEnum.SUCCESS.getCode());
  }

  private void listWitnesses() {
    WitnessList result = walletApiWrapper.listWitnesses();
    if (result != null) {
      logger.info(Utils.printWitnessList(result));
    } else {
      logger.info("List witnesses " + " failed !!");
    }
  }

  private void getAssetIssueList() {
    AssetIssueList result = walletApiWrapper.getAssetIssueList();
    if (result != null) {
      if (walletApiWrapper.isMainChain()) {
        logger.info(Utils.printAssetIssueList(result));
      } else {
        logger.info(Utils.printSideChainAssetIssueList(result));
      }
    } else {
      logger.info("GetAssetIssueList " + " failed !!");
    }
  }

  private void getAssetIssueList(String[] parameters) {
    if (parameters == null || parameters.length != 2) {
      System.out.println(
          "The listassetissuepaginated command needs 2 parameters, use the following syntax:");
      System.out.println("listassetissuepaginated offset limit ");
      return;
    }
    int offset = Integer.parseInt(parameters[0]);
    int limit = Integer.parseInt(parameters[1]);
    AssetIssueList result = walletApiWrapper.getAssetIssueList(offset, limit);
    if (result != null) {
      logger.info(Utils.printAssetIssueList(result));
    } else {
      logger.info("GetAssetIssueListPaginated " + " failed !!");
    }
  }

  private void getProposalsListPaginated(String[] parameters) {
    if (parameters == null || parameters.length != 2) {
      System.out.println(
          "The listproposalspaginated command needs 2 parameters, use the following syntax:");
      System.out.println("listproposalspaginated offset limit ");
      return;
    }
    int offset = Integer.parseInt(parameters[0]);
    int limit = Integer.parseInt(parameters[1]);
    Optional<ProposalList> result = walletApiWrapper.getProposalListPaginated(offset, limit);
    if (result.isPresent()) {
      ProposalList proposalList = result.get();
      logger.info(Utils.printProposalsList(proposalList));
    } else {
      logger.info("listproposalspaginated " + " failed !!");
    }
  }

  private void getExchangesListPaginated(String[] parameters) {
    if (parameters == null || parameters.length != 2) {
      System.out.println(
          "The listexchangespaginated command needs 2 parameters, use the following syntax:");
      System.out.println("listexchangespaginated offset limit ");
      return;
    }
    int offset = Integer.parseInt(parameters[0]);
    int limit = Integer.parseInt(parameters[1]);
    Optional<ExchangeList> result = walletApiWrapper.getExchangeListPaginated(offset, limit);
    if (result.isPresent()) {
      ExchangeList exchangeList = result.get();
      logger.info(Utils.printExchangeList(exchangeList));
    } else {
      logger.info("listexchangespaginated " + " failed !!");
    }
  }


  private void listNodes() {
    Optional<NodeList> result = walletApiWrapper.listNodes();
    if (result.isPresent()) {
      NodeList nodeList = result.get();
      List<Node> list = nodeList.getNodesList();
      for (int i = 0; i < list.size(); i++) {
        Node node = list.get(i);
        logger.info("IP::" + ByteArray.toStr(node.getAddress().getHost().toByteArray()));
        logger.info("Port::" + node.getAddress().getPort());
      }
    } else {
      logger.info("GetAssetIssueList " + " failed !!");
    }
  }

  private void getBlock(String[] parameters) {
    long blockNum = -1;

    if (parameters == null || parameters.length == 0) {
      System.out.println("Get current block !!!!");
    } else {
      if (parameters.length != 1) {
        System.out.println("Getblock has too many parameters !!!");
        System.out.println("You can get current block using the following command:");
        System.out.println("Getblock");
        System.out.println("Or get block by number with the following syntax:");
        System.out.println("Getblock BlockNum");
      }
      blockNum = Long.parseLong(parameters[0]);
    }

    BlockExtention blockExtention = walletApiWrapper.getBlock(blockNum);
    if (blockExtention == null) {
      System.out.println("No block for num : " + blockNum);
      return;
    }
    System.out.println(Utils.printBlockExtention(blockExtention));

  }

  private void getTransactionCountByBlockNum(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("Too many parameters !!!");
      System.out.println("You need input number with the following syntax:");
      System.out.println("GetTransactionCountByBlockNum number");
    }
    long blockNum = Long.parseLong(parameters[0]);
    long count = walletApiWrapper.getTransactionCountByBlockNum(blockNum);
    System.out.println("The block contain " + count + " transactions");
  }

  private void voteWitness(String[] parameters)
      throws IOException, CipherException, CancelException {
    if (parameters == null || parameters.length < 2 || (parameters.length & 1) != 0) {
      System.out.println("Use VoteWitness command with below syntax: ");
      System.out.println("VoteWitness Address0 Count0 ... AddressN CountN");
      return;
    }

    HashMap<String, String> witness = new HashMap<String, String>();
    for (int i = 0; i < parameters.length; i += 2) {
      String address = parameters[i];
      String countStr = parameters[i + 1];
      witness.put(address, countStr);
    }

    SunNetworkResponse<TransactionResponse> result = walletApiWrapper.voteWitness(witness);
    if (checkResult(result)) {
      logger.info("VoteWitness " + " successful !!");
    } else {
      logger.info("VoteWitness " + " failed !!");
    }
  }

  private void freezeBalance(String[] parameters) {
    if (parameters == null || !(parameters.length == 2 || parameters.length == 3
        || parameters.length == 4)) {
      System.out.println("Use freezeBalance command with below syntax: ");
      System.out
          .println(
              "freezeBalance frozen_balance frozen_duration [ResourceCode:0 BANDWIDTH,1 ENERGY] "
                  + "[receiverAddress]");
      return;
    }

    long frozen_balance = Long.parseLong(parameters[0]);
    long frozen_duration = Long.parseLong(parameters[1]);
    int resourceCode = 0;
    String receiverAddress = null;
    if (parameters.length == 3) {
      try {
        resourceCode = Integer.parseInt(parameters[2]);
      } catch (NumberFormatException e) {
        receiverAddress = parameters[2];
      }
    }
    if (parameters.length == 4) {
      resourceCode = Integer.parseInt(parameters[2]);
      receiverAddress = parameters[3];
    }
    SunNetworkResponse<TransactionResponse> result = walletApiWrapper.freezeBalance(frozen_balance,
        frozen_duration, resourceCode, receiverAddress);
    if (checkResult(result)) {
      logger.info("freezeBalance " + " successful !!");
    } else {
      logger.info("freezeBalance " + " failed !!");
    }
  }

  private void unfreezeBalance(String[] parameters)
      throws IOException, CipherException, CancelException {
    if (parameters == null || parameters.length > 2) {
      System.out.println("Use unfreezeBalance command with below syntax: ");
      System.out.println("unfreezeBalance  [ResourceCode:0 BANDWIDTH,1 CPU]" + "[receiverAddress]");
      return;
    }

    int resourceCode = 0;
    String receiverAddress = null;

    if (parameters.length == 1) {
      try {
        resourceCode = Integer.parseInt(parameters[0]);
      } catch (Exception ex) {
        receiverAddress = parameters[0];
      }
    }

    if (parameters.length == 2) {
      resourceCode = Integer.parseInt(parameters[0]);
      receiverAddress = parameters[1];
    }

    boolean result = walletApiWrapper.unfreezeBalance(resourceCode, receiverAddress);
    if (result) {
      logger.info("unfreezeBalance " + " successful !!");
    } else {
      logger.info("unfreezeBalance " + " failed !!");
    }
  }

  private void fundInject(String[] parameters) {
    if (parameters == null || parameters.length < 1) {
      System.out.println("Use fundInject command with below syntax: ");
      System.out.println("fundInject  amount");
      return;
    }

    long amount = Long.parseLong(parameters[0]);

    boolean result = walletApiWrapper.fundInject(amount);
    if (result) {
      logger.info("fundInject " + " successful !!");
    } else {
      logger.info("fundInject " + " failed !!");
    }
  }

  private void unfreezeAsset() {
    boolean result = walletApiWrapper.unfreezeAsset();
    if (result) {
      logger.info("unfreezeAsset " + " successful !!");
    } else {
      logger.info("unfreezeAsset " + " failed !!");
    }
  }

  private void createProposal(String[] parameters) {
    if (parameters == null || parameters.length < 2 || (parameters.length & 1) != 0) {
      System.out.println("Use createProposal command with below syntax: ");
      System.out.println("createProposal id0 value0 ... idN valueN");
      return;
    }

    HashMap<Long, Long> parametersMap = new HashMap<>();
    for (int i = 0; i < parameters.length; i += 2) {
      long id = Long.valueOf(parameters[i]);
      long value = Long.valueOf(parameters[i + 1]);
      parametersMap.put(id, value);
    }
    boolean result = walletApiWrapper.createProposal(parametersMap);
    if (result) {
      logger.info("createProposal " + " successful !!");
    } else {
      logger.info("createProposal " + " failed !!");
    }
  }


  private void sideChainCreateProposal(String[] parameters) {
    if (parameters == null || parameters.length < 2 || (parameters.length & 1) != 0) {
      System.out.println("Use createProposal command with below syntax: ");
      System.out.println("createProposal id0 value0 ... idN valueN");
      return;
    }

    HashMap<Long, String> parametersMap = new HashMap<>();
    for (int i = 0; i < parameters.length; i += 2) {
      long id = Long.valueOf(parameters[i]);
      String value = parameters[i + 1];
      parametersMap.put(id, value);
    }
    boolean result = walletApiWrapper.sideChainCreateProposal(parametersMap);
    if (result) {
      logger.info("createProposal " + " successful !!");
    } else {
      logger.info("createProposal " + " failed !!");
    }
  }

  private void approveProposal(String[] parameters)
      throws IOException, CipherException, CancelException {
    if (parameters == null || parameters.length != 2) {
      System.out.println("Use approveProposal command with below syntax: ");
      System.out.println("approveProposal id is_or_not_add_approval");
      return;
    }

    long id = Long.valueOf(parameters[0]);
    boolean is_add_approval = Boolean.valueOf(parameters[1]);
    boolean result = walletApiWrapper.approveProposal(id, is_add_approval);
    if (result) {
      logger.info("approveProposal " + " successful !!");
    } else {
      logger.info("approveProposal " + " failed !!");
    }
  }

  private void deleteProposal(String[] parameters)
      throws IOException, CipherException, CancelException {
    if (parameters == null || parameters.length != 1) {
      System.out.println("Use deleteProposal command with below syntax: ");
      System.out.println("deleteProposal proposalId");
      return;
    }

    long id = Long.valueOf(parameters[0]);
    boolean result = walletApiWrapper.deleteProposal(id);
    if (result) {
      logger.info("deleteProposal " + " successful !!");
    } else {
      logger.info("deleteProposal " + " failed !!");
    }
  }


  private void listProposals() {
    Optional<ProposalList> result = walletApiWrapper.getProposalsList();
    if (result.isPresent()) {
      ProposalList proposalList = result.get();
      logger.info(Utils.printProposalsList(proposalList));
    } else {
      logger.info("List witnesses " + " failed !!");
    }
  }

  private void sideChainListProposals() {
    Optional<SideChainProposalList> result = walletApiWrapper.sideChainGetProposalsList();
    if (result.isPresent()) {
      SideChainProposalList proposalList = result.get();
      logger.info(Utils.sideChianPrintProposalsList(proposalList));
    } else {
      logger.info("List witnesses " + " failed !!");
    }
  }

  private void getProposal(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("getProposal needs 1 parameter like following: ");
      System.out.println("getProposal id ");
      return;
    }
    String id = parameters[0];

    Optional<Proposal> result = walletApiWrapper.getProposal(id);
    if (result.isPresent()) {
      Proposal proposal = result.get();
      logger.info(Utils.printProposal(proposal));
    } else {
      logger.info("getProposal " + " failed !!");
    }
  }


  private void getDelegatedResource(String[] parameters) {
    if (parameters == null || parameters.length != 2) {
      System.out.println("Use getDelegatedResource command with below syntax: ");
      System.out.println("getDelegatedResource fromAddress toAddress");
      return;
    }
    String fromAddress = parameters[0];
    String toAddress = parameters[1];
    Optional<DelegatedResourceList> result = walletApiWrapper
        .getDelegatedResource(fromAddress, toAddress);
    if (result.isPresent()) {
      DelegatedResourceList delegatedResourceList = result.get();
      logger.info(Utils.printDelegatedResourceList(delegatedResourceList));
    } else {
      logger.info("getDelegatedResource " + " failed !!");
    }
  }

  private void getDelegatedResourceAccountIndex(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("Use getDelegatedResourceAccountIndex command with below syntax: ");
      System.out.println("getDelegatedResourceAccountIndex address ");
      return;
    }
    String address = parameters[0];
    Optional<DelegatedResourceAccountIndex> result = walletApiWrapper
        .getDelegatedResourceAccountIndex(address);
    if (result.isPresent()) {
      DelegatedResourceAccountIndex delegatedResourceAccountIndex = result.get();
      logger.info(Utils.printDelegatedResourceAccountIndex(delegatedResourceAccountIndex));
    } else {
      logger.info("getDelegatedResourceAccountIndex " + " failed !!");
    }
  }


  private void exchangeCreate(String[] parameters) {
    if (parameters == null || parameters.length != 4) {
      System.out.println("Use exchangeCreate command with below syntax: ");
      System.out.println("exchangeCreate first_token_id first_token_balance "
          + "second_token_id second_token_balance");
      return;
    }

    String firstTokenId = parameters[0];
    long firstTokenBalance = Long.parseLong(parameters[1]);
    String secondTokenId = parameters[2];
    long secondTokenBalance = Long.parseLong(parameters[3]);
    boolean result = walletApiWrapper.exchangeCreate(firstTokenId, firstTokenBalance,
        secondTokenId, secondTokenBalance);
    if (result) {
      logger.info("exchange create " + " successful !!");
    } else {
      logger.info("exchange create " + " failed !!");
    }
  }

  private void exchangeInject(String[] parameters) {
    if (parameters == null || parameters.length != 3) {
      System.out.println("Use exchangeInject command with below syntax: ");
      System.out.println("exchangeInject exchange_id token_id quant");
      return;
    }

    long exchangeId = Long.valueOf(parameters[0]);
    String tokenId = parameters[1];
    long quant = Long.valueOf(parameters[2]);
    boolean result = walletApiWrapper.exchangeInject(exchangeId, tokenId, quant);
    if (result) {
      logger.info("exchange inject " + " successful !!");
    } else {
      logger.info("exchange inject " + " failed !!");
    }
  }

  private void exchangeWithdraw(String[] parameters)
      throws IOException, CipherException, CancelException {
    if (parameters == null || parameters.length != 3) {
      System.out.println("Use exchangeWithdraw command with below syntax: ");
      System.out.println("exchangeWithdraw exchange_id token_id quant");
      return;
    }

    long exchangeId = Long.valueOf(parameters[0]);
    String tokenId = parameters[1];
    long quant = Long.valueOf(parameters[2]);
    boolean result = walletApiWrapper.exchangeWithdraw(exchangeId, tokenId, quant);
    if (result) {
      logger.info("exchange withdraw " + " successful !!");
    } else {
      logger.info("exchange withdraw " + " failed !!");
    }
  }

  private void exchangeTransaction(String[] parameters)
      throws IOException, CipherException, CancelException {
    if (parameters == null || parameters.length != 4) {
      System.out.println("Use exchangeTransaction command with below syntax: ");
      System.out.println("exchangeTransaction exchange_id token_id quant expected");
      return;
    }

    long exchangeId = Long.valueOf(parameters[0]);
    String tokenId = parameters[1];
    long quant = Long.valueOf(parameters[2]);
    long expected = Long.valueOf(parameters[3]);
    boolean result = walletApiWrapper.exchangeTransaction(exchangeId, tokenId, quant, expected);
    if (result) {
      logger.info("exchange Transaction " + " successful !!");
    } else {
      logger.info("exchange Transaction " + " failed !!");
    }
  }

  private void listExchanges() {
    Optional<ExchangeList> result = walletApiWrapper.getExchangeList();
    if (result.isPresent()) {
      ExchangeList exchangeList = result.get();
      logger.info(Utils.printExchangeList(exchangeList));
    } else {
      logger.info("List exchanges " + " failed !!");
    }
  }

  private void getExchange(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("getExchange needs 1 parameter like following: ");
      System.out.println("getExchange id ");
      return;
    }
    String id = parameters[0];

    Optional<Exchange> result = walletApiWrapper.getExchange(id);
    if (result.isPresent()) {
      Exchange exchange = result.get();
      logger.info(Utils.printExchange(exchange));
    } else {
      logger.info("getExchange " + " failed !!");
    }
  }

  private void withdrawBalance() {
    boolean result = walletApiWrapper.withdrawBalance();
    if (result) {
      logger.info("withdrawBalance " + " successful !!");
    } else {
      logger.info("withdrawBalance " + " failed !!");
    }
  }

  private void getTotalTransaction() {
    NumberMessage totalTransition = walletApiWrapper.getTotalTransaction();
    logger.info("The num of total transactions is : " + totalTransition.getNum());
  }

  private void getNextMaintenanceTime() {
    String nextMaintenanceTime = walletApiWrapper.getNextMaintenanceTime();

    logger.info("Next maintenance time is : " + nextMaintenanceTime);
  }

  private void getTransactionById(String[] parameters) {
    String txid = "";
    if (parameters == null || parameters.length != 1) {
      System.out.println("getTransactionById needs 1 parameter, transaction id");
      return;
    } else {
      txid = parameters[0];
    }
    Optional<Transaction> result = walletApiWrapper.getTransactionById(txid);
    if (result.isPresent()) {
      Transaction transaction = result.get();
      logger.info(Utils.printTransaction(transaction));
    } else {
      logger.info("getTransactionById " + " failed !!");
    }
  }

  private void getTransactionInfoById(String[] parameters) {
    String txid = "";
    if (parameters == null || parameters.length != 1) {
      System.out.println("getTransactionInfoById needs 1 parameter, transaction id");
      return;
    } else {
      txid = parameters[0];
    }
    Optional<TransactionInfo> result = walletApiWrapper.getTransactionInfoById(txid);
    if (result.isPresent()) {
      TransactionInfo transactionInfo = result.get();
      logger.info(Utils.printTransactionInfo(transactionInfo));
    } else {
      logger.info("getTransactionInfoById " + " failed !!");
    }
  }

  private void getTransactionsFromThis(String[] parameters) {
    if (parameters == null || parameters.length != 3) {
      System.out.println("GetTransactionsFromThis needs 3 parameters, use the following syntax: ");
      System.out.println("GetTransactionsFromThis Address offset limit");
      return;
    }
    String address = parameters[0];
    int offset = Integer.parseInt(parameters[1]);
    int limit = Integer.parseInt(parameters[2]);

    Optional<TransactionListExtention> result = walletApiWrapper
        .getTransactionsFromThis(address, offset, limit);
    if (result.isPresent()) {
      TransactionListExtention transactionList = result.get();
      if (transactionList.getTransactionCount() == 0) {
        System.out.println("No transaction from " + address);
        return;
      }
      System.out.println(Utils.printTransactionList(transactionList));
    } else {
      System.out.println("GetTransactionsFromThis " + " failed !!");
    }

  }

  private void getTransactionsToThis(String[] parameters) {
    if (parameters == null || parameters.length != 3) {
      System.out.println("getTransactionsToThis needs 3 parameters, use the following syntax: ");
      System.out.println("getTransactionsToThis Address offset limit");
      return;
    }
    String address = parameters[0];
    int offset = Integer.parseInt(parameters[1]);
    int limit = Integer.parseInt(parameters[2]);

    Optional<TransactionListExtention> result = walletApiWrapper
        .getTransactionsToThis(address, offset, limit);
    if (result.isPresent()) {
      TransactionListExtention transactionList = result.get();
      if (transactionList.getTransactionCount() == 0) {
        System.out.println("No transaction to " + address);
        return;
      }
      System.out.println(Utils.printTransactionList(transactionList));
    } else {
      System.out.println("getTransactionsToThis " + " failed !!");
    }

  }

  private void getBlockById(String[] parameters) {
    String blockID = "";
    if (parameters == null || parameters.length != 1) {
      System.out.println("getBlockById needs 1 parameter, block id which is hex format");
      return;
    } else {
      blockID = parameters[0];
    }
    Optional<Block> result = walletApiWrapper.getBlockById(blockID);
    if (result.isPresent()) {
      Block block = result.get();
      logger.info(Utils.printBlock(block));
    } else {
      logger.info("getBlockById " + " failed !!");
    }
  }

  private void getBlockByLimitNext(String[] parameters) {
    long start = 0;
    long end = 0;
    if (parameters == null || parameters.length != 2) {
      System.out
          .println("GetBlockByLimitNext needs 2 parameters, start block id and end block id");
      return;
    } else {
      start = Long.parseLong(parameters[0]);
      end = Long.parseLong(parameters[1]);
    }

    Optional<BlockListExtention> result = walletApiWrapper.getBlockByLimitNext(start, end);
    if (result.isPresent()) {
      BlockListExtention blockList = result.get();
      System.out.println(Utils.printBlockList(blockList));
    } else {
      System.out.println("GetBlockByLimitNext " + " failed !!");
    }
  }

  private void getBlockByLatestNum(String[] parameters) {
    long num = 0;
    if (parameters == null || parameters.length != 1) {
      System.out.println("getBlockByLatestNum needs 1 parameter, block num");
      return;
    } else {
      num = Long.parseLong(parameters[0]);
    }
    Optional<BlockListExtention> result = walletApiWrapper.getBlockByLatestNum(num);
    if (result.isPresent()) {
      BlockListExtention blockList = result.get();
      if (blockList.getBlockCount() == 0) {
        System.out.println("No block");
        return;
      }
      System.out.println(Utils.printBlockList(blockList));
    } else {
      System.out.println("GetBlockByLimitNext " + " failed !!");
    }
  }

  private void updateSetting(String[] parameters)
      throws IOException, CipherException, CancelException {
    if (parameters == null ||
        parameters.length < 2) {
      System.out.println("updateSetting needs 2 parameters like following: ");
      System.out.println("updateSetting contract_address consume_user_resource_percent");
      return;
    }

    String contractAddress = parameters[0];
    long consumeUserResourcePercent = Long.valueOf(parameters[1]).longValue();
    if (consumeUserResourcePercent > 100 || consumeUserResourcePercent < 0) {
      System.out.println("consume_user_resource_percent must >= 0 and <= 100");
      return;
    }
    boolean result = walletApiWrapper.updateSetting(contractAddress, consumeUserResourcePercent);
    if (result) {
      System.out.println("update setting successfully");
    } else {
      System.out.println("update setting failed");
    }
  }

  private void updateEnergyLimit(String[] parameters)
      throws IOException, CipherException, CancelException {
    if (parameters == null ||
        parameters.length < 2) {
      System.out.println("updateEnergyLimit needs 2 parameters like following: ");
      System.out.println("updateEnergyLimit contract_address energy_limit");
      return;
    }

    String contractAddress = parameters[0];
    long originEnergyLimit = Long.valueOf(parameters[1]).longValue();
    if (originEnergyLimit < 0) {
      System.out.println("origin_energy_limit need > 0 ");
      return;
    }
    boolean result = walletApiWrapper.updateEnergyLimit(contractAddress, originEnergyLimit);
    if (result) {
      System.out.println("update setting for origin_energy_limit successfully");
    } else {
      System.out.println("update setting for origin_energy_limit failed");
    }
  }

  private String[] getParas(String[] para) {
    String paras = String.join(" ", para);
    Pattern pattern = Pattern.compile(" (\\[.*?\\]) ");
    Matcher matcher = pattern.matcher(paras);

    if (matcher.find()) {
      String ABI = matcher.group(1);
      List<String> tempList = new ArrayList<String>();

      paras = paras.replaceAll("(\\[.*?\\]) ", "");

      String[] parts = paras.split(" ");
      for (int i = 0; i < parts.length; i++) {
        if (1 == i) {
          tempList.add(ABI);
        }
        tempList.add(parts[i]);
      }
      return tempList.toArray(new String[0]);

    } else {
      return null;
    }

  }

  private void deployContract(String[] parameter) throws EncodingException {

    String[] parameters = getParas(parameter);
    if (parameters == null ||
        parameters.length < 11) {
      System.out.println("DeployContract needs at least 8 parameters like following: ");
      System.out.println(
          "DeployContract contractName ABI byteCode constructor params isHex fee_limit consume_user_resource_percent origin_energy_limit value token_value token_id(e.g: TRXTOKEN, use # if don't provided) <library:address,library:address,...> <lib_compiler_version(e.g:v5)>");
      System.out.println(
          "Note: Please append the param for constructor tightly with byteCode without any space");
      return;
    }
    int idx = 0;
    String contractName = parameters[idx++];
    String abiStr = parameters[idx++];
    String codeStr = parameters[idx++];
    String constructorStr = parameters[idx++];
    String argsStr = parameters[idx++];
    boolean isHex = Boolean.parseBoolean(parameters[idx++]);
    long feeLimit = Long.parseLong(parameters[idx++]);
    long consumeUserResourcePercent = Long.parseLong(parameters[idx++]);
    long originEnergyLimit = Long.parseLong(parameters[idx++]);
    if (consumeUserResourcePercent > 100 || consumeUserResourcePercent < 0) {
      System.out.println("consume_user_resource_percent should be >= 0 and <= 100");
      return;
    }
    if (originEnergyLimit <= 0) {
      System.out.println("origin_energy_limit must > 0");
      return;
    }
    if (!constructorStr.equals("#")) {
      if (isHex) {
        codeStr += argsStr;
      } else {
        codeStr += ByteArray.toHexString(AbiUtil.encodeInput(constructorStr, argsStr));
      }
    }
    long value = 0;
    value = Long.valueOf(parameters[idx++]);
    long tokenValue = Long.valueOf(parameters[idx++]);
    String tokenId = parameters[idx++];
    if (tokenId == "#") {
      tokenId = "";
    }
    String libraryAddressPair = null;
    if (parameters.length > idx) {
      libraryAddressPair = parameters[idx++];
    }

    String compilerVersion = null;
    if (parameters.length > idx) {
      compilerVersion = parameters[idx];
    }

    // TODO: consider to remove "data"
    /* Consider to move below null value, since we append the constructor param just after bytecode without any space.
     * Or we can re-design it to give other developers better user experience. Set this value in protobuf as null for now.
     */
    boolean result = walletApiWrapper
        .deployContract(contractName, abiStr, codeStr, constructorStr, argsStr, isHex, feeLimit,
            value,
            consumeUserResourcePercent, originEnergyLimit, tokenValue, tokenId, libraryAddressPair,
            compilerVersion);
    if (result) {
      System.out.println("Broadcast the createSmartContract successfully.\n"
          + "Please check the given transaction id to confirm deploy status on blockchain using getTransactionInfoById command.");
    } else {
      System.out.println("Broadcast the createSmartContract failed");
    }
  }

  private void triggerContract(String[] parameters) {
    if (parameters == null ||
        parameters.length < 8) {
      System.out.println("TriggerContract needs 8 parameters like following: ");
      System.out.println(
          "TriggerContract contractAddress method args isHex fee_limit value token_value token_id(e.g: TRXTOKEN, use # if don't provided)");
      // System.out.println("example:\nTriggerContract password contractAddress method args value");
      return;
    }

    String contractAddrStr = parameters[0];
    String methodStr = parameters[1];
    String argsStr = parameters[2];
    boolean isHex = Boolean.valueOf(parameters[3]);
    long feeLimit = Long.valueOf(parameters[4]);
    long callValue = Long.valueOf(parameters[5]);
    long tokenCallValue = Long.valueOf(parameters[6]);
    String tokenId = parameters[7];
    if (argsStr.equalsIgnoreCase("#")) {
      argsStr = "";
    }
    if (tokenId.equalsIgnoreCase("#")) {
      tokenId = "";
    }

    boolean result = walletApiWrapper
        .callContract(contractAddrStr, callValue, methodStr, argsStr, isHex, feeLimit,
            tokenCallValue, tokenId);
    if (result) {
      System.out.println("Broadcast the triggerContract successfully.\n"
          + "Please check the given transaction id to get the result on blockchain using getTransactionInfoById command");
    } else {
      System.out.println("Broadcast the triggerContract failed");
    }
  }

  private void triggerConstantContract(String[] parameters) {
    if (parameters == null ||
        parameters.length < 5) {
      System.out.println("TriggerConstantContract needs 5 parameters like following: ");
      System.out.println(
          "TriggerConstantContract contractAddress method args isHex fee_limit");
      // System.out.println("example:\nTriggerContract password contractAddress method args value");
      return;
    }

    String contractAddrStr = parameters[0];
    String methodStr = parameters[1];
    String argsStr = parameters[2];
    boolean isHex = Boolean.valueOf(parameters[3]);
    long feeLimit = Long.valueOf(parameters[4]);
    if (argsStr.equalsIgnoreCase("#")) {
      argsStr = "";
    }

    boolean result = walletApiWrapper
        .callConstantContract(contractAddrStr, methodStr, argsStr, isHex, feeLimit);
    if (result) {
      System.out.println("Broadcast the TriggerConstantContract successfully.");
    } else {
      System.out.println("Broadcast the TriggerConstantContract failed");
    }
  }

  private void getContract(String[] parameters) {
    if (parameters == null ||
        parameters.length != 1) {
      System.out.println("GetContract needs 1 parameter like following: ");
      System.out.println("GetContract contractAddress");
      return;
    }

    String address = parameters[0];
    SmartContract contractDeployContract = walletApiWrapper.getContract(address);
    if (contractDeployContract != null) {
      System.out.println("contract :" + contractDeployContract.getAbi().toString());
      System.out.println("contract owner:" + AddressUtil.encode58Check(contractDeployContract
          .getOriginAddress().toByteArray()));
      System.out.println("contract ConsumeUserResourcePercent:" + contractDeployContract
          .getConsumeUserResourcePercent());
      System.out.println("contract energy limit:" + contractDeployContract
          .getOriginEnergyLimit());
    } else {
      System.out.println("query contract failed!");
    }
  }

  private void generateAddress() {
    AddressPrKeyPairMessage result = walletApiWrapper.generateAddress();
    if (null != result) {
      System.out.println("Address: " + result.getAddress());
      System.out.println("PrivateKey: " + result.getPrivateKey());
      logger.info("GenerateAddress " + " successful !!");
    } else {
      logger.info("GenerateAddress " + " failed !!");
    }
  }

  private void generateAddressOffline() {
    AddressPrKeyPairMessage result = walletApiWrapper.generateAddressOffline();
    if (null != result) {
      System.out.println("Address: " + result.getAddress());
      System.out.println("PrivateKey: " + result.getPrivateKey());
      logger.info("GenerateAddress " + " successful !!");
    } else {
      logger.info("GenerateAddress " + " failed !!");
    }
  }


  private void updateAccountPermission(String[] parameters)
      throws CipherException, IOException, CancelException {
    if (parameters == null || parameters.length != 2) {
      System.out.println(
          "UpdateAccountPermission needs 2 parameters, like UpdateAccountPermission ownerAddress permissions, permissions is json format");
      return;
    }

    String ownerAddress = parameters[0];

    boolean ret = walletApiWrapper.accountPermissionUpdate(ownerAddress, parameters[1]);
    if (ret) {
      logger.info("updateAccountPermission successful !!!!");
    } else {
      logger.info("updateAccountPermission failed !!!!");
    }
  }


  private void getTransactionSignWeight(String[] parameters) throws InvalidProtocolBufferException {
    if (parameters == null || parameters.length != 1) {
      System.out.println(
          "getTransactionSignWeight needs 1 parameter, like getTransactionSignWeight transaction which is hex string");
      return;
    }

    String transactionStr = parameters[0];

    TransactionSignWeight transactionSignWeight = walletApiWrapper
        .getTransactionSignWeight(transactionStr);
    if (transactionSignWeight != null) {
      logger.info(Utils.printTransactionSignWeight(transactionSignWeight));
    } else {
      logger.info("GetTransactionSignWeight failed !!");
    }
  }

  private void getTransactionApprovedList(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println(
          "getTransactionApprovedList needs 1 parameter, like getTransactionApprovedList transaction which is hex string");
      return;
    }

    String transactionStr = parameters[0];

    TransactionApprovedList transactionApprovedList = walletApiWrapper
        .getTransactionApprovedList(transactionStr);
    if (transactionApprovedList != null) {
      logger.info(Utils.printTransactionApprovedList(transactionApprovedList));
    } else {
      logger.info("GetTransactionApprovedList failed !!");
    }
  }

  private void addTransactionSign(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println(
          "addTransactionSign needs 1 parameter, like addTransactionSign transaction which is hex string");
      return;
    }

    String transactionStr = parameters[0];
    Transaction transaction;

    transaction = walletApiWrapper.addTransactionSign(transactionStr);
    if (transaction != null) {
      System.out.println(Utils.printTransaction(transaction));
      System.out
          .println("Transaction hex string is " + ByteArray
              .toHexString(transaction.toByteArray()));
    } else {
      logger.info("AddTransactionSign failed !!");
    }

  }

  private void broadcastTransaction(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println(
          "broadcastTransaction needs 1 parameter, like broadcastTransaction transaction which is hex string");
      return;
    }

    String transactionStr = parameters[0];

    TransactionResponse ret = walletApiWrapper.broadcastTransaction(transactionStr);
    if (ret.result) {
      logger.info("BroadcastTransaction successful !!!!");
    } else {
      logger.info("BroadcastTransaction failed !!!!");
    }

  }

  private void create2(String[] parameters) {
    if (parameters == null || parameters.length != 3) {
      System.out.println("create2 needs 3 parameter: ");
      System.out.println("create2 address code salt");
      return;
    }

    byte[] address = AddressUtil.decodeFromBase58Check(parameters[0]);
    if (!AddressUtil.addressValid(address)) {
      System.out.println("length of address must be 21 bytes.");
      return;
    }

    byte[] code = ByteArray.fromHexString(parameters[1]);
    byte[] temp = Longs.toByteArray(Long.parseLong(parameters[2]));
    if (temp.length != 8) {
      System.out.println("invalid salt!");
      return;
    }
    byte[] salt = new byte[32];
    System.arraycopy(temp, 0, salt, 24, 8);

    byte[] mergedData = ByteUtil.merge(address, salt, Hash.sha3(code));
    String Address = AddressUtil.encode58Check(Hash.sha3omit12(mergedData));

    System.out.println("create2 Address: " + Address);

    return;
  }

  private void depositTrx(String[] parameters) {
    if (parameters == null || parameters.length != 4) {
      System.out.println("deposit trx needs 3 parameters like following: ");
      System.out.println("deposit trx num depositFee feeLimit");
      return;
    }

    long callValue = Long.valueOf(parameters[1]);
    long depositFee = Long.valueOf(parameters[2]);
    long feeLimit = Long.valueOf(parameters[3]);

    SunNetworkResponse<TransactionResponse> resp = walletApiWrapper
        .depositTrx(callValue, depositFee, feeLimit);
    if (checkResult(resp)) {
      System.out.println("deposit trx success");
      System.out.println(
          "Please check the given transaction id to confirm deposit status on blockchain using getTransactionInfoById command.");

    } else {
      System.out.println("deposit trx failed");
    }
  }

  private void depositTrc10(String[] parameters) {
    if (parameters == null || parameters.length != 5) {
      System.out.println("deposit trc10 needs 4 parameters like following: ");
      System.out.println("deposit trc10 trc10id num depositFee feeLimit");
      return;
    }

    String tokenId = parameters[1];

    long tokenCallValue = Long.valueOf(parameters[2]);
    long depositFee = Long.valueOf(parameters[3]);
    long feeLimit = Long.valueOf(parameters[4]);

    SunNetworkResponse<TransactionResponse> resp = walletApiWrapper
        .depositTrc10(tokenId, tokenCallValue, depositFee, feeLimit);
    if (checkResult(resp)) {
      System.out.println("deposit trc10 success");
      System.out.println(
          "Please check the given transaction id to confirm deposit status on blockchain using getTransactionInfoById command.");

    } else {
      System.out.println("deposit trc10 failed");
    }
  }

  private void depositTrc20(String[] parameters) {
    if (parameters == null || parameters.length != 5) {
      System.out.println("deposit trc20 needs 4 parameters like following: ");
      System.out.println("deposit trc20 trc20ContractAddress num depositFee feeLimit");
      return;
    }

    String contractAddrStr = parameters[1];  //main trc20 contract address
    String num = parameters[2];
    long depositFee = Long.valueOf(parameters[3]);
    long feeLimit = Long.valueOf(parameters[4]);

    SunNetworkResponse<TransactionResponse> resp = walletApiWrapper
        .depositTrc20(contractAddrStr, num, depositFee, feeLimit);
    if (checkResult(resp)) {
      System.out.println("deposit trc20 success");
      System.out.println(
          "Please check the given transaction id to confirm deposit status on blockchain using getTransactionInfoById command.");
    } else {
      System.out.println("deposit trc20 failed");
    }
  }

  private void depositTrc721(String[] parameters) {
    if (parameters == null || parameters.length != 5) {
      System.out.println("deposit trc721 needs 4 parameters like following: ");
      System.out.println("deposit trc721 trc721ContractAddress tokenId depositFee feeLimit");
      return;
    }

    String contractAddrStr = parameters[1];  //main trc20 contract address
    String num = parameters[2];
    long depositFee = Long.valueOf(parameters[3]);
    long feeLimit = Long.valueOf(parameters[4]);
    SunNetworkResponse<TransactionResponse> resp = walletApiWrapper
        .depositTrc721(contractAddrStr, num, depositFee, feeLimit);
    if (checkResult(resp)) {
      System.out.println("deposit trc20 success");
      System.out.println(
          "Please check the given transaction id to confirm deposit status on blockchain using getTransactionInfoById command.");

    } else {
      System.out.println("deposit trc20 failed");
    }

  }

  private void deposit(String[] parameters) {
    if (parameters == null || parameters.length < 1) {
      System.out.println("deposit needs parameters (trx| trc10| trc20| trc721)");
      return;
    }

    String type = parameters[0];
    switch (type.toLowerCase()) {
      case "trx": {
        depositTrx(parameters);
        break;
      }
      case "trc10": {
        depositTrc10(parameters);
        break;
      }
      case "trc20": {
        depositTrc20(parameters);
        break;
      }
      case "trc721": {
        depositTrc721(parameters);
        break;
      }
      default: {
        System.out.println("Invalid deposit type: " + type + "!!");
      }
    }

  }

  private void help() {
    System.out.println("Help: List of Tron Wallet-cli commands");
    System.out.println(
        "For more information on a specific command, type the command and it will display tips");
    System.out.println("");

    List<String> allCmds = getAllMainCmds();
    for (String cmd : allCmds) {
      System.out.println(cmd);
    }

    System.out.println("Input any one of the listed commands, to display how-to tips.");
  }

  private void sideHelp() {
    System.out.println("Help: List of Tron Sum-cli sidechain commands");
    System.out.println(
        "For more information on a specific command, type the command and it will display tips");
    System.out.println("");

    List<String> allCmds = getAllSideCmds();

    for (String cmd : allCmds) {
      System.out.println(cmd);
    }

    return;
  }

  private void hintCmd(String input, List<String> allCmds) {
    List<String> ret = Similarity.getSimilarWordList(input, allCmds);
    if (ret.size() == 0) {
      System.out.println("there is no similar command, see help.");
      return;
    }

    System.out.println("the command " + input + " is not exist, similar cmd is: ");
    for (String similarCmd : ret) {
      System.out.println("  " + similarCmd);
    }

    return;
  }

  private List<String> getAllMainCmds() {
    List<String> allCmds = new ArrayList<String>();
    allCmds.add("help");
    allCmds.add("switchtoside");
    allCmds.add("ss");
    allCmds.add("registerwallet");
    allCmds.add("importwallet");
    allCmds.add("importwalletbybase64");
    allCmds.add("changepassword");
    allCmds.add("login");
    allCmds.add("logout");
    allCmds.add("backupwallet");
    allCmds.add("backupwallet2base64");
    allCmds.add("getaddress");
    allCmds.add("getbalance");
    allCmds.add("getaccount");
    allCmds.add("getaccountbyid");
    allCmds.add("updateaccount");
    allCmds.add("setaccountid");
    allCmds.add("updateasset");
    allCmds.add("getassetissuebyaccount");
    allCmds.add("getaccountnet");
    allCmds.add("getaccountresource");
    allCmds.add("getassetissuebyname");
    allCmds.add("getassetissuelistbyname");
    allCmds.add("getassetissuebyid");
    allCmds.add("sendcoin");
    allCmds.add("testtransaction");
    allCmds.add("transferasset");
    allCmds.add("participateassetissue");
    allCmds.add("assetissue");
    allCmds.add("createaccount");
    allCmds.add("createwitness");
    allCmds.add("updatewitness");
    allCmds.add("votewitness");
    allCmds.add("freezebalance");
    allCmds.add("unfreezebalance");
    allCmds.add("withdrawbalance");
    allCmds.add("unfreezeasset");
    allCmds.add("createproposal");
    allCmds.add("approveproposal");
    allCmds.add("deleteproposal");
    allCmds.add("listproposals");
    allCmds.add("listproposalspaginated");
    allCmds.add("getproposal");
    allCmds.add("getdelegatedresource");
    allCmds.add("getdelegatedresourceaccountindex");
    allCmds.add("exchangecreate");
    allCmds.add("exchangeinject");
    allCmds.add("exchangewithdraw");
    allCmds.add("exchangetransaction");
    allCmds.add("listexchanges");
    allCmds.add("listexchangespaginated");
    allCmds.add("getexchange");
    allCmds.add("getchainparameters");
    allCmds.add("listwitnesses");
    allCmds.add("listassetissue");
    allCmds.add("listassetissuepaginated");
    allCmds.add("listnodes");
    allCmds.add("getblock");
    allCmds.add("gettransactioncountbyblocknum");
    allCmds.add("gettotaltransaction");
    allCmds.add("getnextmaintenancetime");
    allCmds.add("gettransactionsfromthis");
    allCmds.add("gettransactionstothis");
    allCmds.add("gettransactionbyid");
    allCmds.add("gettransactioninfobyid");
    allCmds.add("getblockbyid");
    allCmds.add("getblockbylimitnext");
    allCmds.add("getblockbylatestnum");
    allCmds.add("updatesetting");
    allCmds.add("updateenergylimit");
    allCmds.add("deploycontract");
    allCmds.add("triggercontract");
    allCmds.add("triggerconstantcontract");
    allCmds.add("getcontract");
    allCmds.add("generateaddress");
    allCmds.add("generateaddressoffline");
    allCmds.add("updateaccountpermission");
    allCmds.add("gettransactionsignweight");
    allCmds.add("gettransactionapprovedlist");
    allCmds.add("addtransactionsign");
    allCmds.add("broadcasttransaction");
    allCmds.add("create2");
    allCmds.add("deposit");
    allCmds.add("mapping");
    allCmds.add("retry");
    allCmds.add("exit");
    allCmds.add("quit");

    Collections.sort(allCmds, new Comparator<String>() {
      public int compare(String arg0, String arg1) {
        return arg0.compareTo(arg1);
      }
    });

    return allCmds;
  }

  private void hintCmdMain(String input) {
    List<String> allCmds = getAllMainCmds();
    hintCmd(input, allCmds);
  }


  private List<String> getAllSideCmds() {
    List<String> allCmds = new ArrayList<String>();
    allCmds.add("help");
    allCmds.add("importwallet");
    allCmds.add("importwalletbybase64");
    allCmds.add("switchtomain");
    allCmds.add("sm");
    allCmds.add("login");
    allCmds.add("logout");
    allCmds.add("getaddress");
    allCmds.add("getbalance");
    allCmds.add("getaccount");
    allCmds.add("updateaccount");
    allCmds.add("gettransactioncountbyblocknum");
    allCmds.add("getaccountresource");
    allCmds.add("getassetissuebyid");
    allCmds.add("sendcoin");
    allCmds.add("transferasset");
    allCmds.add("createaccount");
    allCmds.add("createwitness");
    allCmds.add("updatewitness");
    allCmds.add("votewitness");
    allCmds.add("freezebalance");
    allCmds.add("unfreezebalance");
    allCmds.add("fundinject");
    allCmds.add("withdrawbalance");
    allCmds.add("listproposals");
    allCmds.add("getproposal");
    allCmds.add("getchainparameters");
    allCmds.add("listwitnesses");
    allCmds.add("listassetissue");
    allCmds.add("listnodes");
    allCmds.add("getblock");
    allCmds.add("gettransactionbyid");
    allCmds.add("gettransactionsfromthis");
    allCmds.add("gettransactionstothis");
    allCmds.add("gettransactioninfobyid");
    allCmds.add("getblockbyid");
    allCmds.add("updatesetting");
    allCmds.add("updateenergylimit");
    allCmds.add("getcontract");
    allCmds.add("triggercontract");
    allCmds.add("triggerconstantcontract");
    allCmds.add("deploycontract");
    allCmds.add("approveproposal");
    allCmds.add("deleteproposal");
    allCmds.add("withdraw");
    allCmds.add("retry");
    allCmds.add("createproposal");
    allCmds.add("getmappingaddress");
    allCmds.add("getnextmaintenancetime");
    allCmds.add("updateaccountpermission");
    allCmds.add("gettransactionsignweight");
    allCmds.add("gettransactionapprovedlist");
    allCmds.add("addtransactionsign");
    allCmds.add("broadcasttransaction");
    allCmds.add("generateaddressoffline");
    allCmds.add("exit");
    allCmds.add("quit");

    Collections.sort(allCmds, new Comparator<String>() {
      public int compare(String arg0, String arg1) {
        return arg0.compareTo(arg1);
      }
    });

    return allCmds;
  }

  private void hintCmdSide(String input) {
    List<String> allCmds = getAllSideCmds();

    hintCmd(input, allCmds);
  }

  private String[] getCmd(String cmdLine) {
    if (cmdLine.indexOf("\"") < 0 || cmdLine.toLowerCase().startsWith("deploycontract")
        || cmdLine.toLowerCase().startsWith("triggercontract")
        || cmdLine.toLowerCase().startsWith("triggerconstantcontract")
        || cmdLine.toLowerCase().startsWith("updateaccountpermission")) {
      return cmdLine.split("\\s+");
    }
    String[] strArray = cmdLine.split("\"");
    int num = strArray.length;
    int start = 0;
    int end = 0;
    if (cmdLine.charAt(0) == '\"') {
      start = 1;
    }
    if (cmdLine.charAt(cmdLine.length() - 1) == '\"') {
      end = 1;
    }
    if (((num + end) & 1) == 0) {
      return new String[]{"ErrorInput"};
    }

    List<String> cmdList = new ArrayList<>();
    for (int i = start; i < strArray.length; i++) {
      if ((i & 1) == 0) {
        cmdList.addAll(Arrays.asList(strArray[i].trim().split("\\s+")));
      } else {
        cmdList.add(strArray[i].trim());
      }
    }
    Iterator ito = cmdList.iterator();
    while (ito.hasNext()) {
      if (ito.next().equals("")) {
        ito.remove();
      }
    }
    String[] result = new String[cmdList.size()];
    return cmdList.toArray(result);
  }

  private boolean runMain(String cmd, String[] parameters) {

    try {

      String cmdLowerCase = cmd.toLowerCase();

      switch (cmdLowerCase) {
        case "help": {
          help();
          break;
        }
        case "ss":
        case "switchtoside": {
          switch2Side();
          break;
        }
        case "registerwallet": {
          registerWallet();
          break;
        }
        case "importwallet": {
          importWallet();
          break;
        }
        case "importwalletbybase64": {
          importwalletByBase64();
          break;
        }
        case "changepassword": {
          changePassword();
          break;
        }
        case "login": {
          login();
          break;
        }
        case "logout": {
          logout();
          break;
        }
        case "backupwallet": {
          backupWallet();
          break;
        }
        case "backupwallet2base64": {
          backupWallet2Base64();
          break;
        }
        case "getaddress": {
          getAddress();
          break;
        }
        case "getbalance": {
          getBalance();
          break;
        }
        case "getaccount": {
          getAccount(parameters);
          break;
        }
        case "getaccountbyid": {
          getAccountById(parameters);
          break;
        }
        case "updateaccount": {
          updateAccount(parameters);
          break;
        }
        case "setaccountid": {
          setAccountId(parameters);
          break;
        }
        case "updateasset": {
          updateAsset(parameters);
          break;
        }
        case "getassetissuebyaccount": {
          getAssetIssueByAccount(parameters);
          break;
        }
        case "getaccountnet": {
          getAccountNet(parameters);
          break;
        }
        case "getaccountresource": {
          getAccountResource(parameters);
          break;
        }
        case "getassetissuebyname": {
          getAssetIssueByName(parameters);
          break;
        }
        case "getassetissuelistbyname": {
          getAssetIssueListByName(parameters);
          break;
        }
        case "getassetissuebyid": {
          getAssetIssueById(parameters);
          break;
        }
        case "sendcoin": {
          sendCoin(parameters);
          break;
        }
        case "testtransaction": {
          testTransaction(parameters);
          break;
        }
        case "transferasset": {
          transferAsset(parameters);
          break;
        }
        case "participateassetissue": {
          participateAssetIssue(parameters);
          break;
        }
        case "assetissue": {
          assetIssue(parameters);
          break;
        }
        case "createaccount": {
          createAccount(parameters);
          break;
        }
        case "createwitness": {
          createWitness(parameters);
          break;
        }
        case "updatewitness": {
          updateWitness(parameters);
          break;
        }
        case "votewitness": {
          voteWitness(parameters);
          break;
        }
        case "freezebalance": {
          freezeBalance(parameters);
          break;
        }
        case "unfreezebalance": {
          unfreezeBalance(parameters);
          break;
        }
        case "withdrawbalance": {
          withdrawBalance();
          break;
        }
        case "unfreezeasset": {
          unfreezeAsset();
          break;
        }
        case "createproposal": {
          createProposal(parameters);
          break;
        }
        case "approveproposal": {
          approveProposal(parameters);
          break;
        }
        case "deleteproposal": {
          deleteProposal(parameters);
          break;
        }
        case "listproposals": {
          listProposals();
          break;
        }
        case "listproposalspaginated": {
          getProposalsListPaginated(parameters);
          break;
        }
        case "getproposal": {
          getProposal(parameters);
          break;
        }
        case "getdelegatedresource": {
          getDelegatedResource(parameters);
          break;
        }
        case "getdelegatedresourceaccountindex": {
          getDelegatedResourceAccountIndex(parameters);
          break;
        }
        case "exchangecreate": {
          exchangeCreate(parameters);
          break;
        }
        case "exchangeinject": {
          exchangeInject(parameters);
          break;
        }
        case "exchangewithdraw": {
          exchangeWithdraw(parameters);
          break;
        }
        case "exchangetransaction": {
          exchangeTransaction(parameters);
          break;
        }
        case "listexchanges": {
          listExchanges();
          break;
        }
        case "listexchangespaginated": {
          getExchangesListPaginated(parameters);
          break;
        }
        case "getexchange": {
          getExchange(parameters);
          break;
        }
        case "getchainparameters": {
          getMainChainParameters();
          break;
        }
        case "listwitnesses": {
          listWitnesses();
          break;
        }
        case "listassetissue": {
          getAssetIssueList();
          break;
        }
        case "listassetissuepaginated": {
          getAssetIssueList(parameters);
          break;
        }
        case "listnodes": {
          listNodes();
          break;
        }
        case "getblock": {
          getBlock(parameters);
          break;
        }
        case "gettransactioncountbyblocknum": {
          getTransactionCountByBlockNum(parameters);
          break;
        }
        case "gettotaltransaction": {
          getTotalTransaction();
          break;
        }
        case "getnextmaintenancetime": {
          getNextMaintenanceTime();
          break;
        }
        case "gettransactionsfromthis": {
          getTransactionsFromThis(parameters);
          break;
        }
        case "gettransactionstothis": {
          getTransactionsToThis(parameters);
          break;
        }
        case "gettransactionbyid": {
          getTransactionById(parameters);
          break;
        }
        case "gettransactioninfobyid": {
          getTransactionInfoById(parameters);
          break;
        }
        case "getblockbyid": {
          getBlockById(parameters);
          break;
        }
        case "getblockbylimitnext": {
          getBlockByLimitNext(parameters);
          break;
        }
        case "getblockbylatestnum": {
          getBlockByLatestNum(parameters);
          break;
        }
        case "updatesetting": {
          updateSetting(parameters);
          break;
        }
        case "updateenergylimit": {
          updateEnergyLimit(parameters);
          break;
        }
        case "deploycontract": {
          deployContract(parameters);
          break;
        }
        case "triggercontract": {
          triggerContract(parameters);
          break;
        }
        case "triggerconstantcontract": {
          triggerConstantContract(parameters);
          break;
        }
        case "getcontract": {
          getContract(parameters);
          break;
        }
        case "generateaddress": {
          generateAddress();
          break;
        }
        case "generateaddressoffline": {
          generateAddressOffline();
          break;
        }
        case "updateaccountpermission": {
          updateAccountPermission(parameters);
          break;
        }
        case "gettransactionsignweight": {
          getTransactionSignWeight(parameters);
          break;
        }
        case "gettransactionapprovedlist": {
          getTransactionApprovedList(parameters);
          break;
        }
        case "addtransactionsign": {
          addTransactionSign(parameters);
          break;
        }
        case "broadcasttransaction": {
          broadcastTransaction(parameters);
          break;
        }
        case "create2": {
          create2(parameters);
          break;
        }
        case "deposit": {
          deposit(parameters);
          break;
        }
        case "mapping": {
          mapping(parameters);
          break;
        }
        case "retry": {
          mainRetry(parameters);
          break;
        }
        case "exit":
        case "quit": {
          System.out.println("Exit !!!");
          return true;
        }
        default: {
          System.out.println("Invalid cmd: " + cmd);
          hintCmdMain(cmdLowerCase);
        }
      }
    } catch (CipherException e) {
      System.out.println(cmd + " failed!");
      System.out.println(e.getMessage());
    } catch (IOException e) {
      System.out.println(cmd + " failed!");
      System.out.println(e.getMessage());
    } catch (CancelException e) {
      System.out.println(cmd + " failed!");
      System.out.println(e.getMessage());
    } catch (Exception e) {
      System.out.println(cmd + " failed!");
      logger.error(e.getMessage());
      e.printStackTrace();
    }
    return false;
  }


  private void switch2Main() {
    walletApiWrapper.switch2Main();
    System.out.println("Switch successfully.");
    return;
  }


  private void switch2Side() {
    walletApiWrapper.switch2Side();
    System.out.println("Switch successfully.");
    return;
  }

  private void withdrawTrx(String[] parameters) {
    if (parameters == null || parameters.length != 4) {
      System.out.println("withdraw Trx needs 3 parameters like following: ");
      System.out.println("withdraw Trx trx_num withdrawFee fee_limit ");
      return;
    }

    long trxNum = Long.parseLong(parameters[1]);
    long withdrawFee = Long.parseLong(parameters[2]);
    long feeLimit = Long.parseLong(parameters[3]);

    SunNetworkResponse<TransactionResponse> resp = walletApiWrapper
        .withdrawTrx(trxNum, withdrawFee, feeLimit);
    if (checkResult(resp)) {
      System.out.println("withdraw trx success");
      System.out.println(
          "Please check the given transaction id to confirm withdraw status on blockchain using getTransactionInfoById command.");

    } else {
      System.out.println("withdraw trx failed");
    }

    return;
  }

  private void withdrawTrc10(String[] parameters) {
    if (parameters == null || parameters.length != 5) {
      System.out.println("withdraw trc10 needs 4 parameters like following: ");
      System.out.println("withdraw trc10 trc10Id value withdrawFee fee_limit ");
      return;
    }

    String trc10 = parameters[1];
    String value = parameters[2];
    long withdrawFee = Long.parseLong(parameters[3]);
    long feeLimit = Long.parseLong(parameters[4]);
    long tokenValue = Long.parseLong(value);

    SunNetworkResponse<TransactionResponse> resp = walletApiWrapper
        .withdrawTrc10(trc10, tokenValue, withdrawFee, feeLimit);
    if (checkResult(resp)) {
      System.out.println("withdraw trc10 success");
      System.out.println(
          "Please check the given transaction id to confirm withdraw status on blockchain using getTransactionInfoById command.");

    } else {
      System.out.println("withdraw trc10 failed");
    }
  }

  private void withdrawTrc20(String[] parameters) {
    if (parameters == null || parameters.length != 5) {
      System.out.println("withdraw Trc20 needs 4 parameters like following: ");
      System.out.println("withdraw Trc20 sideTrc20Address value withdrawFee fee_limit ");
      return;
    }

    String sideTrc20Address = parameters[1]; //sidechain trc20 address
    String value = parameters[2];
    long withdrawFee = Long.parseLong(parameters[3]);
    long feeLimit = Long.parseLong(parameters[4]);

    SunNetworkResponse<TransactionResponse> resp = walletApiWrapper
        .withdrawTrc20(sideTrc20Address, value, withdrawFee, feeLimit);
    if (checkResult(resp)) {
      System.out.println("withdraw trc20 success");
      System.out.println(
          "Please check the given transaction id to confirm withdraw status on blockchain using getTransactionInfoById command.");

    } else {
      System.out.println("withdraw trc20 failed");
    }

    return;
  }

  private void withdrawTrc721(String[] parameters) {
    if (parameters == null || parameters.length != 5) {
      System.out.println("withdraw Trc721 needs 4 parameters like following: ");
      System.out.println("withdraw Trc721 sideTrc721Address uid withdrawFee fee_limit ");
      return;
    }

    String sideTrc721Address = parameters[1]; //sidechain trc721 address
    String uid = parameters[2];
    long withdrawFee = Long.parseLong(parameters[3]);
    long feeLimit = Long.parseLong(parameters[4]);

    SunNetworkResponse<TransactionResponse> resp = walletApiWrapper
        .withdrawTrc721(sideTrc721Address, uid, withdrawFee, feeLimit);
    if (checkResult(resp)) {
      System.out.println("withdraw trc721 success");
      System.out.println(
          "Please check the given transaction id to confirm withdraw status on blockchain using getTransactionInfoById command.");

    } else {
      System.out.println("withdraw trc721 failed");
    }

    return;
  }


  private void withdraw(String[] parameters) {
    if (parameters == null || parameters.length < 1) {
      System.out.println("withdraw needs parameters (trx|trc10|trc20|trc721)");
      return;
    }

    String type = parameters[0];
    switch (type.toLowerCase()) {
      case "trx": {
        withdrawTrx(parameters);
        break;
      }
      case "trc10": {
        withdrawTrc10(parameters);
        break;
      }
      case "trc20": {
        withdrawTrc20(parameters);
        break;
      }
      case "trc721": {
        withdrawTrc721(parameters);
        break;
      }
      default: {
        System.out.println("Invalid withdraw type: " + type + "!!");
      }
    }
  }

  private void retryDeposit(String[] parameters) {
    if (parameters == null || parameters.length != 4) {
      System.out.println("retry deposit needs 3 parameters like following: ");
      System.out.println("retry deposit nonce retryFee fee_limit ");
      return;
    }

    String nonce = parameters[1];
    long retryFee = Long.parseLong(parameters[2]);
    long feeLimit = Long.parseLong(parameters[3]);

    SunNetworkResponse<TransactionResponse> resp = walletApiWrapper
        .retryDeposit(nonce, retryFee, feeLimit);
    if (checkResult(resp)) {
      System.out.println("retry deposit success");
      System.out.println(
          "Please check the given transaction id to confirm retry status on blockchain using getTransactionInfoById command.");

    } else {
      System.out.println("retry deposit failed");
    }

    return;
  }

  private void retryWithdraw(String[] parameters) {
    if (parameters == null || parameters.length != 4) {
      System.out.println("retry withdraw needs 3 parameters like following: ");
      System.out.println("retry withdraw nonce retryFee fee_limit ");
      return;
    }

    String nonce = parameters[1];
    long retryFee = Long.parseLong(parameters[2]);
    long feeLimit = Long.parseLong(parameters[3]);

    SunNetworkResponse<TransactionResponse> resp = walletApiWrapper
        .retryWithdraw(nonce, retryFee, feeLimit);
    if (checkResult(resp)) {
      System.out.println("retry withdraw success");
      System.out.println(
          "Please check the given transaction id to confirm retry status on blockchain using getTransactionInfoById command.");

    } else {
      System.out.println("retry withdraw failed");
    }

    return;
  }

  private void retryMapping(String[] parameters) {
    if (parameters == null || parameters.length != 4) {
      System.out.println("retry mapping needs 3 parameters like following: ");
      System.out.println("retry mapping nonce retryFee, fee_limit ");
      return;
    }

    String nonce = parameters[1];
    long retryFee = Long.parseLong(parameters[2]);
    long feeLimit = Long.parseLong(parameters[3]);

    SunNetworkResponse<TransactionResponse> resp = walletApiWrapper
        .retryMapping(nonce, retryFee, feeLimit);
    if (checkResult(resp)) {
      System.out.println("retry mapping success");
      System.out.println(
          "Please check the given transaction id to confirm retry status on blockchain using getTransactionInfoById command.");

    } else {
      System.out.println("retry mapping failed");
    }

    return;
  }

  private void mainRetry(String[] parameters) {
    if (parameters == null || parameters.length < 1) {
      System.out.println("retry needs parameters (deposit |mapping )");
      return;
    }

    String type = parameters[0];
    switch (type.toLowerCase()) {
      case "deposit": {
        retryDeposit(parameters);
        break;
      }
      case "mapping": {
        retryMapping(parameters);
        break;
      }
      default: {
        System.out.println("Invalid main chain retry type: " + type + "!!");
      }
    }
  }

  private void sideRetry(String[] parameters) {
    if (parameters == null || parameters.length < 1) {
      System.out.println("retry needs parameters (withdraw)");
      return;
    }

    String type = parameters[0];
    switch (type.toLowerCase()) {
      case "withdraw": {
        retryWithdraw(parameters);
        break;
      }
      default: {
        System.out.println("Invalid side chain retry type: " + type + "!!");
      }
    }
  }

  private void getMappingAddress(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("getmappingaddress needs 1 parameters like following: ");
      System.out.println("getmappingaddress mainContractAddress");
      return;
    }

    byte[] sideGateway = ServerApi.getSideGatewayAddress();

    String mainContractAddress = parameters[0];

    walletApiWrapper.sideGetMappingAddress(sideGateway, mainContractAddress);
  }

  private void mappingTrc20(String[] parameters) {
    if (parameters == null || parameters.length != 4) {
      System.out.println("mapping trc20 needs 3 parameters like following: ");
      System.out.println("mapping trc20  trxHash mappingFee feeLimit");
      return;
    }

    String trxHash = parameters[1];
    long mappingFee = Long.valueOf(parameters[2]);
    long feeLimit = Long.valueOf(parameters[3]);

    SunNetworkResponse<TransactionResponse> resp = walletApiWrapper
        .mappingTrc20(trxHash, mappingFee, feeLimit);
    if (checkResult(resp)) {
      System.out.println("mapping trc20 success");
      System.out.println(
          "Please check the given transaction id to confirm mapping status on blockchain using getTransactionInfoById command.");

    }

    System.out.println(
        "please use getMappingAddress to confirm the result in side chain solidity node after 60s.");
  }

  private void mappingTrc721(String[] parameters) {
    if (parameters == null || parameters.length != 4) {
      System.out.println("mapping trc721 needs 3 parameters like following: ");
      System.out.println("mapping trc721  trxHash mappingFee feeLimit");
      return;
    }

    String trxHash = parameters[1];
    long mappingFee = Long.valueOf(parameters[2]);
    long feeLimit = Long.valueOf(parameters[3]);

    SunNetworkResponse<TransactionResponse> resp = walletApiWrapper
        .mappingTrc721(trxHash, mappingFee, feeLimit);
    if (checkResult(resp)) {
      System.out.println("mapping trc721 success");
      System.out.println(
          "Please check the given transaction id to confirm mapping status on blockchain using getTransactionInfoById command.");

    }

    System.out.println(
        "please use getMappingAddress to confirm the result in side chain solidity node after 60s.");
  }

  private void mapping(String[] parameters) {
    if (parameters == null || parameters.length < 1) {
      System.out.println("mapping needs parameters (trc20 |trc721 )");
      return;
    }

    String type = parameters[0];
    switch (type.toLowerCase()) {
      case "trc20": {
        mappingTrc20(parameters);
        break;
      }
      case "trc721": {
        mappingTrc721(parameters);
        break;
      }
      default: {
        System.out.println("Invalid mapping type: " + type + "!!");
      }
    }
  }

  private boolean runSide(String cmd, String[] parameters) {

    try {
      String cmdLowerCase = cmd.toLowerCase();

      switch (cmdLowerCase) {
        case "sm":
        case "switchtomain": {
          switch2Main();
          break;
        }
        case "importwallet": {
          importWallet();
          break;
        }
        case "importwalletbybase64": {
          importwalletByBase64();
          break;
        }
        case "help": {
          sideHelp();
          break;
        }
        case "login": {
          login();
          break;
        }
        case "logout": {
          logout();
          break;
        }
        case "getaddress": {
          getAddress();
          break;
        }
        case "getbalance": {
          getBalance();
          break;
        }
        case "getaccount": {
          getAccount(parameters);
          break;
        }
        case "updateaccount": {
          updateAccount(parameters);
          break;
        }
        case "getaccountresource": {
          getAccountResource(parameters);
          break;
        }
        case "getassetissuebyid": {
          getAssetIssueById(parameters);
          break;
        }
        case "sendcoin": {
          sendCoin(parameters);
          break;
        }
        case "transferasset": {
          transferAsset(parameters);
          break;
        }
        case "createaccount": {
          createAccount(parameters);
          break;
        }
        case "createwitness": {
          createWitness(parameters);
          break;
        }
        case "updatewitness": {
          updateWitness(parameters);
          break;
        }
        case "votewitness": {
          voteWitness(parameters);
          break;
        }
        case "freezebalance": {
          freezeBalance(parameters);
          break;
        }
        case "unfreezebalance": {
          unfreezeBalance(parameters);
          break;
        }
        case "fundinject": {
          fundInject(parameters);
          break;
        }
        case "withdrawbalance": {
          withdrawBalance();
          break;
        }
        case "listproposals": {
          sideChainListProposals();
          break;
        }
        case "getproposal": {
          getProposal(parameters);
          break;
        }
        case "getchainparameters": {
          getSideChainParameters();
          break;
        }
        case "listwitnesses": {
          listWitnesses();
          break;
        }
        case "listassetissue": {
          getAssetIssueList();
          break;
        }
        case "listnodes": {
          listNodes();
          break;
        }
        case "getblock": {
          getBlock(parameters);
          break;
        }
        case "gettransactioncountbyblocknum": {
          getTransactionCountByBlockNum(parameters);
          break;
        }
        case "gettransactionbyid": {
          getTransactionById(parameters);
          break;
        }
        case "gettransactionsfromthis": {
          getTransactionsFromThis(parameters);
          break;
        }
        case "gettransactionstothis": {
          getTransactionsToThis(parameters);
          break;
        }
        case "gettransactioninfobyid": {
          getTransactionInfoById(parameters);
          break;
        }
        case "getblockbyid": {
          getBlockById(parameters);
          break;
        }
        case "updatesetting": {
          updateSetting(parameters);
          break;
        }
        case "updateenergylimit": {
          updateEnergyLimit(parameters);
          break;
        }
        case "getcontract": {
          getContract(parameters);
          break;
        }
        case "triggercontract": {
          triggerContract(parameters);
          break;
        }
        case "triggerconstantcontract": {
          triggerConstantContract(parameters);
          break;
        }
        case "deploycontract": {
          deployContract(parameters);

          break;
        }
        case "approveproposal": {
          approveProposal(parameters); //TODO: account change？
          break;

        }
        case "deleteproposal": {
          deleteProposal(parameters);
          break;
        }
        case "withdraw": {
          withdraw(parameters);
          break;
        }
        case "retry": {
          sideRetry(parameters);
          break;
        }
        case "getmappingaddress": {
          getMappingAddress(parameters);
          break;
        }
        case "createproposal": {
          sideChainCreateProposal(parameters);
          break;
        }
        case "getnextmaintenancetime": {
          getNextMaintenanceTime();
          break;
        }
        case "updateaccountpermission": {
          updateAccountPermission(parameters);
          break;
        }
        case "gettransactionsignweight": {
          getTransactionSignWeight(parameters);
          break;
        }
        case "gettransactionapprovedlist": {
          getTransactionApprovedList(parameters);
          break;
        }
        case "addtransactionsign": {
          addTransactionSign(parameters);
          break;
        }
        case "broadcasttransaction": {
          broadcastTransaction(parameters);
          break;
        }
        case "generateaddressoffline": {
          generateAddressOffline();
          break;
        }
        case "exit":
        case "quit": {
          System.out.println("Exit !!!");
          return true;
        }
        default: {
          System.out.println("Invalid cmd: " + cmd);
          hintCmdSide(cmdLowerCase);
        }
      }
    } catch (CipherException e) {
      System.out.println(cmd + " failed!");
      System.out.println(e.getMessage());
    } catch (IOException e) {
      System.out.println(cmd + " failed!");
      System.out.println(e.getMessage());
    } catch (CancelException e) {
      System.out.println(cmd + " failed!");
      System.out.println(e.getMessage());
    } catch (Exception e) {
      System.out.println(cmd + " failed!");
      logger.error(e.getMessage());
      e.printStackTrace();
    }
    return false;
  }


  private void run() {
    Scanner in = new Scanner(System.in);
    System.out.println(" ");
    System.out.println("Welcome to Tron Sun-Cli main chain");
    System.out.println("Please type one of the following commands to proceed.");
    System.out.println("Login, RegisterWallet or ImportWallet");
    System.out.println(" ");
    System.out.println(
        "You may also use the Help command at anytime to display a full list of commands.");
    System.out.println(" ");

    System.out.println("[mainchain] ");
    while (in.hasNextLine()) {
      String cmd = "";
      String cmdLine = in.nextLine().trim();
      String[] cmdArray = getCmd(cmdLine);

      cmd = cmdArray[0];
      if ("".equals(cmd)) {
        if (walletApiWrapper.isMainChain()) {
          System.out.println("[mainchain] ");
        } else {
          System.out.println("[sidechain] ");
        }
        continue;
      }
      String[] parameters = Arrays.copyOfRange(cmdArray, 1, cmdArray.length);

      boolean ret = false;
      if (walletApiWrapper.isMainChain()) {
        ret = runMain(cmd, parameters);
      } else {
        ret = runSide(cmd, parameters);
      }

      if (ret == true) {
        return;
      }

      //System.out.println();
      if (walletApiWrapper.isMainChain()) {
        System.out.println("[mainchain] ");
      } else {
        System.out.println("[sidechain] ");
      }
      in = new Scanner(System.in);
    }
  }

  private void getMainChainParameters() {
    Optional<ChainParameters> result = walletApiWrapper.getChainParameters();
    if (result.isPresent()) {
      ChainParameters chainParameters = result.get();
      logger.info(Utils.printChainParameters(chainParameters));
    } else {
      logger.info("List Chain Parameters " + " failed !!");
    }
  }

  private void getSideChainParameters() {
    Optional<SideChainParameters> result = walletApiWrapper.getSideChainParameters();
    if (result.isPresent()) {
      SideChainParameters sideChainParameters = result.get();
      logger.info(Utils.printChainParameters(sideChainParameters));
    } else {
      logger.info("List Chain Parameters " + " failed !!");
    }
  }

  public static void main(String[] args) {
    Client cli = new Client();
    JCommander.newBuilder()
        .addObject(cli)
        .build()
        .parse(args);

    cli.run();
  }
}
