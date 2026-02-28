package stest.tron.wallet.dailybuild.tvmnewcommand.transferfailed;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.GrpcAPI.Return.response_code;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class TransferFailed005 {

  private final String testNetAccountKey = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] testNetAccountAddress = PublicMethedForDailybuild
      .getFinalAddress(testNetAccountKey);
  private final Long maxFeeLimit = Configuration.getByPath("testng.cong")
      .getLong("defaultParameter.maxFeeLimit");
  byte[] contractAddress = null;
  byte[] contractAddress1 = null;
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] accountExcAddress = ecKey1.getAddress();
  String accountExcKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  private ManagedChannel channelSolidity = null;
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull1 = null;
  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;
  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);

  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  @BeforeClass(enabled = true)
  public void beforeClass() {
    PublicMethedForDailybuild.printAddress(accountExcKey);
    channelFull = ManagedChannelBuilder.forTarget(fullnode).usePlaintext(true).build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1).usePlaintext(true).build();
    blockingStubFull1 = WalletGrpc.newBlockingStub(channelFull1);

    {
      Assert.assertTrue(PublicMethedForDailybuild
          .sendcoin(accountExcAddress, 10000_000_000L, testNetAccountAddress, testNetAccountKey,
              blockingStubFull));
      PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

      String filePath = "src/test/resources/soliditycode/TransferFailed005.sol";
      String contractName = "EnergyOfTransferFailedTest";
      HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
      String code = retMap.get("byteCode").toString();
      String abi = retMap.get("abI").toString();

      contractAddress = PublicMethedForDailybuild
          .deployContract(contractName, abi, code, "", maxFeeLimit, 100L, 100L, null, accountExcKey,
              accountExcAddress, blockingStubFull);

      filePath = "src/test/resources/soliditycode/TransferFailed005.sol";
      contractName = "Caller";
      retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
      code = retMap.get("byteCode").toString();
      abi = retMap.get("abI").toString();

      contractAddress1 = PublicMethedForDailybuild
          .deployContract(contractName, abi, code, "", maxFeeLimit, 0L, 100L, null, accountExcKey,
              accountExcAddress, blockingStubFull);
    }
  }

  @Test(enabled = false, description = "Deploy contract for trigger")
  public void deployContract() {
    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(accountExcAddress, 10000_000_000L, testNetAccountAddress, testNetAccountKey,
            blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    String filePath = "src/test/resources/soliditycode/TransferFailed005.sol";
    String contractName = "EnergyOfTransferFailedTest";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit, 0L, 100L, null, accountExcKey,
            accountExcAddress, blockingStubFull);
    String Txid1 = PublicMethedForDailybuild
        .deployContractAndGetTransactionInfoById(contractName, abi, code, "", maxFeeLimit, 0L, 100L,
            null, accountExcKey, accountExcAddress, blockingStubFull);
    Optional<TransactionInfo> infoById = PublicMethedForDailybuild
        .getTransactionInfoById(Txid1, blockingStubFull);
    contractAddress = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());

    filePath = "src/test/resources/soliditycode/TransferFailed005.sol";
    contractName = "Caller";
    retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    code = retMap.get("byteCode").toString();
    abi = retMap.get("abI").toString();

    contractAddress1 = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit, 0L, 100L, null, accountExcKey,
            accountExcAddress, blockingStubFull);
    Txid1 = PublicMethedForDailybuild
        .deployContractAndGetTransactionInfoById(contractName, abi, code, "", maxFeeLimit, 0L, 100L,
            null, accountExcKey, accountExcAddress, blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(Txid1, blockingStubFull);
    contractAddress1 = infoById.get().getContractAddress().toByteArray();
    logger.info("caller address : " + Base58.encode58Check(contractAddress1));
    Assert.assertEquals(0, infoById.get().getResultValue());
  }

  @Test(enabled = true, description = "TransferFailed for function call_value ")
  public void triggerContract01() {
    Account info = null;

    AccountResourceMessage resourceInfo = PublicMethedForDailybuild
        .getAccountResource(accountExcAddress, blockingStubFull);
    info = PublicMethedForDailybuild.queryAccount(accountExcKey, blockingStubFull);
    Long beforeBalance = info.getBalance();
    Long beforeEnergyUsed = resourceInfo.getEnergyUsed();
    Long beforeNetUsed = resourceInfo.getNetUsed();
    Long beforeFreeNetUsed = resourceInfo.getFreeNetUsed();
    logger.info("beforeBalance:" + beforeBalance);
    logger.info("beforeEnergyUsed:" + beforeEnergyUsed);
    logger.info("beforeNetUsed:" + beforeNetUsed);
    logger.info("beforeFreeNetUsed:" + beforeFreeNetUsed);

    //Assert.assertTrue(PublicMethedForDailybuild
    //    .sendcoin(contractAddress, 1000100L, accountExcAddress, accountExcKey, blockingStubFull));
    //Assert.assertTrue(PublicMethedForDailybuild
    //    .sendcoin(contractAddress1, 1, accountExcAddress, accountExcKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    logger.info("contractAddress balance before: " + PublicMethedForDailybuild
        .queryAccount(contractAddress, blockingStubFull).getBalance());
    logger.info("callerAddress balance before: " + PublicMethedForDailybuild
        .queryAccount(contractAddress1, blockingStubFull).getBalance());
    long paramValue = 1;

    // transfer trx to self`s account
    String param = "\"" + paramValue + "\",\"" + Base58.encode58Check(contractAddress) + "\"";
    String triggerTxid = PublicMethedForDailybuild
        .triggerContract(contractAddress, "testCallTrxInsufficientBalance(uint256,address)", param,
            false, 0L, maxFeeLimit, accountExcAddress, accountExcKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = PublicMethedForDailybuild
        .getTransactionInfoById(triggerTxid, blockingStubFull);

    Assert.assertEquals(infoById.get().getResultValue(), 1);
    Assert.assertEquals("FAILED", infoById.get().getResult().toString());
    Assert.assertEquals("TRANSFER_FAILED", infoById.get().getReceipt().getResult().toString());
    Assert.assertEquals("transfer trx failed: Cannot transfer TRX to yourself.",
        infoById.get().getResMessage().toStringUtf8());
    Assert.assertEquals(100L,
        PublicMethedForDailybuild.queryAccount(contractAddress, blockingStubFull).getBalance());
    Assert.assertEquals(0L,
        PublicMethedForDailybuild.queryAccount(contractAddress1, blockingStubFull).getBalance());
    Assert.assertTrue(infoById.get().getReceipt().getEnergyUsageTotal() < 10000000);

    // transfer trx to unactivate account
    ECKey ecKey2 = new ECKey(Utils.getRandom());
    byte[] accountExcAddress2 = ecKey2.getAddress();
    param = "\"" + paramValue + "\",\"" + Base58.encode58Check(accountExcAddress2) + "\"";
    triggerTxid = PublicMethedForDailybuild
        .triggerContract(contractAddress, "testCallTrxInsufficientBalance(uint256,address)", param,
            false, 0L, maxFeeLimit, accountExcAddress, accountExcKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(triggerTxid, blockingStubFull);

    Assert.assertEquals(infoById.get().getResultValue(), 0);
    Assert.assertEquals("SUCESS", infoById.get().getResult().toString());

    Assert.assertEquals(99L,
        PublicMethedForDailybuild.queryAccount(contractAddress, blockingStubFull).getBalance());
    Assert.assertEquals(0L,
        PublicMethedForDailybuild.queryAccount(contractAddress1, blockingStubFull).getBalance());
    Assert.assertTrue(infoById.get().getReceipt().getEnergyUsageTotal() < 10000000);

    // transfer trx to caller, value enough , function success contractResult(call_value) successed
    param = "\"" + paramValue + "\",\"" + Base58.encode58Check(contractAddress1) + "\"";
    triggerTxid = PublicMethedForDailybuild
        .triggerContract(contractAddress, "testCallTrxInsufficientBalance(uint256,address)", param,
            false, 0L, maxFeeLimit, accountExcAddress, accountExcKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    infoById = PublicMethedForDailybuild.getTransactionInfoById(triggerTxid, blockingStubFull);
    logger.info(infoById.get().getReceipt().getResult() + "");

    Long fee = infoById.get().getFee();
    Long netUsed = infoById.get().getReceipt().getNetUsage();
    Long energyUsed = infoById.get().getReceipt().getEnergyUsage();
    Long netFee = infoById.get().getReceipt().getNetFee();
    long energyUsageTotal = infoById.get().getReceipt().getEnergyUsageTotal();
    logger.info("fee:" + fee);
    logger.info("netUsed:" + netUsed);
    logger.info("energyUsed:" + energyUsed);
    logger.info("netFee:" + netFee);
    logger.info("energyUsageTotal:" + energyUsageTotal);

    int contractResult = ByteArray.toInt(infoById.get().getContractResult(0).toByteArray());
    Assert.assertEquals(1, contractResult);

    Assert.assertEquals(infoById.get().getResultValue(), 0);
    Assert.assertEquals(infoById.get().getResult().toString(), "SUCESS");
    Assert.assertEquals(98L,
        PublicMethedForDailybuild.queryAccount(contractAddress, blockingStubFull).getBalance());
    Assert.assertEquals(1L,
        PublicMethedForDailybuild.queryAccount(contractAddress1, blockingStubFull).getBalance());
    Assert.assertTrue(infoById.get().getReceipt().getEnergyUsageTotal() < 10000000);

    // transfer trx to caller, value not enough, function success
    // but contractResult(call_value) failed
    param = "\"" + 100 + "\",\"" + Base58.encode58Check(contractAddress1) + "\"";
    triggerTxid = PublicMethedForDailybuild
        .triggerContract(contractAddress, "testCallTrxInsufficientBalance(uint256,address)", param,
            false, 0L, maxFeeLimit, accountExcAddress, accountExcKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    infoById = PublicMethedForDailybuild.getTransactionInfoById(triggerTxid, blockingStubFull);
    fee = infoById.get().getFee();
    netUsed = infoById.get().getReceipt().getNetUsage();
    energyUsed = infoById.get().getReceipt().getEnergyUsage();
    netFee = infoById.get().getReceipt().getNetFee();
    energyUsageTotal = infoById.get().getReceipt().getEnergyUsageTotal();
    logger.info("fee:" + fee);
    logger.info("netUsed:" + netUsed);
    logger.info("energyUsed:" + energyUsed);
    logger.info("netFee:" + netFee);
    logger.info("energyUsageTotal:" + energyUsageTotal);

    //contractResult`s first boolean value
    contractResult = ByteArray.toInt(infoById.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, contractResult);
    Assert.assertEquals(infoById.get().getResultValue(), 0);
    Assert.assertEquals(infoById.get().getResult().toString(), "SUCESS");
    Assert.assertEquals(98L,
        PublicMethedForDailybuild.queryAccount(contractAddress, blockingStubFull).getBalance());
    Assert.assertEquals(1L,
        PublicMethedForDailybuild.queryAccount(contractAddress1, blockingStubFull).getBalance());
    Assert.assertTrue(infoById.get().getReceipt().getEnergyUsageTotal() < 10000000);


  }

  @Test(enabled = true, description = "TransferFailed for create")
  public void triggerContract02() {
    Long ContractBalance = PublicMethedForDailybuild.queryAccount(contractAddress, blockingStubFull)
        .getBalance();
    Account info = null;

    AccountResourceMessage resourceInfo = PublicMethedForDailybuild
        .getAccountResource(accountExcAddress, blockingStubFull);
    info = PublicMethedForDailybuild.queryAccount(accountExcKey, blockingStubFull);
    Long beforeBalance = info.getBalance();
    Long beforeEnergyUsed = resourceInfo.getEnergyUsed();
    Long beforeNetUsed = resourceInfo.getNetUsed();
    Long beforeFreeNetUsed = resourceInfo.getFreeNetUsed();
    logger.info("beforeBalance:" + beforeBalance);
    logger.info("beforeEnergyUsed:" + beforeEnergyUsed);
    logger.info("beforeNetUsed:" + beforeNetUsed);
    logger.info("beforeFreeNetUsed:" + beforeFreeNetUsed);

    //Assert.assertTrue(PublicMethedForDailybuild
    //    .sendcoin(contractAddress, 1000100L, accountExcAddress, accountExcKey, blockingStubFull));
    //Assert.assertTrue(PublicMethedForDailybuild
    //    .sendcoin(contractAddress1, 1, accountExcAddress, accountExcKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    logger.info("contractAddress balance before: " + PublicMethedForDailybuild
        .queryAccount(contractAddress, blockingStubFull).getBalance());
    logger.info("callerAddress balance before: " + PublicMethedForDailybuild
        .queryAccount(contractAddress1, blockingStubFull).getBalance());
    long paramValue = 1;
    String param = "\"" + paramValue + "\"";

    String triggerTxid = PublicMethedForDailybuild
        .triggerContract(contractAddress, "testCreateTrxInsufficientBalance(uint256)", param, false,
            0L, maxFeeLimit, accountExcAddress, accountExcKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById = PublicMethedForDailybuild
        .getTransactionInfoById(triggerTxid, blockingStubFull);
    logger.info(infoById.get().getReceipt().getResult() + "");

    Long fee = infoById.get().getFee();
    Long netUsed = infoById.get().getReceipt().getNetUsage();
    Long energyUsed = infoById.get().getReceipt().getEnergyUsage();
    Long netFee = infoById.get().getReceipt().getNetFee();
    long energyUsageTotal = infoById.get().getReceipt().getEnergyUsageTotal();
    logger.info("fee:" + fee);
    logger.info("netUsed:" + netUsed);
    logger.info("energyUsed:" + energyUsed);
    logger.info("netFee:" + netFee);
    logger.info("energyUsageTotal:" + energyUsageTotal);

    logger.info("contractAddress balance before: " + PublicMethedForDailybuild
        .queryAccount(contractAddress, blockingStubFull).getBalance());
    logger.info("callerAddress balance before: " + PublicMethedForDailybuild
        .queryAccount(contractAddress1, blockingStubFull).getBalance());
    Assert.assertEquals(infoById.get().getResultValue(), 0);
    Assert.assertFalse(infoById.get().getInternalTransactions(0).getRejected());
    Assert.assertEquals(ContractBalance - 1,
        PublicMethedForDailybuild.queryAccount(contractAddress, blockingStubFull).getBalance());
    Assert.assertTrue(infoById.get().getReceipt().getEnergyUsageTotal() < 10000000);

    param = "\"" + (ContractBalance + 1) + "\"";
    triggerTxid = PublicMethedForDailybuild
        .triggerContract(contractAddress, "testCreateTrxInsufficientBalance(uint256)", param, false,
            0L, maxFeeLimit, accountExcAddress, accountExcKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    infoById = PublicMethedForDailybuild.getTransactionInfoById(triggerTxid, blockingStubFull);
    fee = infoById.get().getFee();
    netUsed = infoById.get().getReceipt().getNetUsage();
    energyUsed = infoById.get().getReceipt().getEnergyUsage();
    netFee = infoById.get().getReceipt().getNetFee();
    energyUsageTotal = infoById.get().getReceipt().getEnergyUsageTotal();
    logger.info("fee:" + fee);
    logger.info("netUsed:" + netUsed);
    logger.info("energyUsed:" + energyUsed);
    logger.info("netFee:" + netFee);
    logger.info("energyUsageTotal:" + energyUsageTotal);

    logger.info("contractAddress balance before: " + PublicMethedForDailybuild
        .queryAccount(contractAddress, blockingStubFull).getBalance());
    logger.info("callerAddress balance before: " + PublicMethedForDailybuild
        .queryAccount(contractAddress1, blockingStubFull).getBalance());

    Assert.assertEquals(infoById.get().getResultValue(), 1);
    Assert.assertEquals(infoById.get().getResMessage().toStringUtf8(), "REVERT opcode executed");
    Assert.assertEquals(ContractBalance - 1,
        PublicMethedForDailybuild.queryAccount(contractAddress, blockingStubFull).getBalance());
    Assert.assertTrue(infoById.get().getReceipt().getEnergyUsageTotal() < 10000000);


  }

  @Test(enabled = true, description = "TransferFailed for create2")
  public void triggerContract03() {
    Long ContractBalance = PublicMethedForDailybuild.queryAccount(contractAddress, blockingStubFull)
        .getBalance();

    Account info;

    AccountResourceMessage resourceInfo = PublicMethedForDailybuild
        .getAccountResource(accountExcAddress, blockingStubFull);
    info = PublicMethedForDailybuild.queryAccount(accountExcKey, blockingStubFull);
    Long beforeBalance = info.getBalance();
    Long beforeEnergyUsed = resourceInfo.getEnergyUsed();
    Long beforeNetUsed = resourceInfo.getNetUsed();
    Long beforeFreeNetUsed = resourceInfo.getFreeNetUsed();
    logger.info("beforeBalance:" + beforeBalance);
    logger.info("beforeEnergyUsed:" + beforeEnergyUsed);
    logger.info("beforeNetUsed:" + beforeNetUsed);
    logger.info("beforeFreeNetUsed:" + beforeFreeNetUsed);

    //Assert.assertTrue(PublicMethedForDailybuild
    //    .sendcoin(contractAddress, 15L, accountExcAddress, accountExcKey, blockingStubFull));
    logger.info("contractAddress balance before: " + PublicMethedForDailybuild
        .queryAccount(contractAddress, blockingStubFull).getBalance());

    String filePath = "./src/test/resources/soliditycode/TransferFailed007.sol";
    String contractName = "Caller";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String testContractCode = retMap.get("byteCode").toString();
    Long salt = 1L;

    String param = "\"" + testContractCode + "\"," + salt;

    String triggerTxid = PublicMethedForDailybuild
        .triggerContract(contractAddress, "deploy(bytes,uint256)", param, false, 0L, maxFeeLimit,
            accountExcAddress, accountExcKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById = PublicMethedForDailybuild
        .getTransactionInfoById(triggerTxid, blockingStubFull);

    Long fee = infoById.get().getFee();
    Long netUsed = infoById.get().getReceipt().getNetUsage();
    Long energyUsed = infoById.get().getReceipt().getEnergyUsage();
    Long netFee = infoById.get().getReceipt().getNetFee();
    long energyUsageTotal = infoById.get().getReceipt().getEnergyUsageTotal();
    logger.info("fee:" + fee);
    logger.info("netUsed:" + netUsed);
    logger.info("energyUsed:" + energyUsed);
    logger.info("netFee:" + netFee);
    logger.info("energyUsageTotal:" + energyUsageTotal);

    Long afterBalance = 0L;
    afterBalance = PublicMethedForDailybuild.queryAccount(contractAddress, blockingStubFull)
        .getBalance();
    logger.info("contractAddress balance after : " + PublicMethedForDailybuild
        .queryAccount(contractAddress, blockingStubFull).getBalance());
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById.get().getResult().toString());
    Assert.assertEquals(ContractBalance - 10L, afterBalance.longValue());
    Assert.assertFalse(infoById.get().getInternalTransactions(0).getRejected());
    Assert.assertTrue(infoById.get().getReceipt().getEnergyUsageTotal() < 10000000);

    triggerTxid = PublicMethedForDailybuild
        .triggerContract(contractAddress, "deploy2(bytes,uint256)", param, false, 0L, maxFeeLimit,
            accountExcAddress, accountExcKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(triggerTxid, blockingStubFull);

    fee = infoById.get().getFee();
    netUsed = infoById.get().getReceipt().getNetUsage();
    energyUsed = infoById.get().getReceipt().getEnergyUsage();
    netFee = infoById.get().getReceipt().getNetFee();
    energyUsageTotal = infoById.get().getReceipt().getEnergyUsageTotal();
    logger.info("fee:" + fee);
    logger.info("netUsed:" + netUsed);
    logger.info("energyUsed:" + energyUsed);
    logger.info("netFee:" + netFee);
    logger.info("energyUsageTotal:" + energyUsageTotal);

    afterBalance = PublicMethedForDailybuild.queryAccount(contractAddress, blockingStubFull)
        .getBalance();
    logger.info("contractAddress balance after : " + PublicMethedForDailybuild
        .queryAccount(contractAddress, blockingStubFull).getBalance());
    Assert.assertEquals(1, infoById.get().getResultValue());
    Assert.assertEquals("FAILED", infoById.get().getResult().toString());
    Assert.assertEquals(ContractBalance - 10L, afterBalance.longValue());
    Assert.assertEquals(0, ByteArray.toInt(infoById.get().getContractResult(0).toByteArray()));
    Assert.assertTrue(infoById.get().getReceipt().getEnergyUsageTotal() < 10000000);

  }

  @Test(enabled = true, description = "Triggerconstant a transfer function")
  public void triggerContract04() {
    Account account = PublicMethedForDailybuild.queryAccount(accountExcAddress, blockingStubFull);
    Account ContractAccount = PublicMethedForDailybuild
        .queryAccount(contractAddress, blockingStubFull);

    Long AccountBeforeBalance = account.getBalance();
    Long ContractAccountBalance = ContractAccount.getBalance();

    TransactionExtention return1 = PublicMethedForDailybuild
        .triggerConstantContractForExtention(contractAddress,
            "testTransferTrxInsufficientBalance(uint256)", "1", false, 0L, 1000000000, "0", 0L,
            accountExcAddress, accountExcKey, blockingStubFull);
    Assert.assertEquals(response_code.SUCCESS, return1.getResult().getCode());
    /*Assert.assertEquals(
        "class org.tron.core.vm.program.Program$StaticCallModificationException "
            + ": Attempt to call a state modifying opcode inside STATICCALL",
        return1.getResult().getMessage().toStringUtf8());*/

    logger.info("return1: " + return1);

    account = PublicMethedForDailybuild.queryAccount(accountExcAddress, blockingStubFull);
    ContractAccount = PublicMethedForDailybuild.queryAccount(contractAddress, blockingStubFull);

    Assert.assertEquals(AccountBeforeBalance.longValue(), account.getBalance());
    Assert.assertEquals(ContractAccountBalance.longValue(), ContractAccount.getBalance());
  }

  /**
   * constructor.
   */
  @AfterClass

  public void shutdown() throws InterruptedException {
    PublicMethedForDailybuild
        .freedResource(accountExcAddress, accountExcKey, testNetAccountAddress, blockingStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelSolidity != null) {
      channelSolidity.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
