package stest.tron.wallet.dailybuild.depositWithdraw;

import static org.tron.api.GrpcAPI.Return.response_code.CONTRACT_VALIDATE_ERROR;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.spongycastle.util.encoders.Hex;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Result.contractResult;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class DepositTrc20002 {


  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] depositAddress2 = ecKey2.getAddress();
  String testKeyFordeposit2 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);
  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelSolidity = null;
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingSideStubFull = null;
  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("mainfullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);

  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  /**
   * constructor.
   */

  @BeforeClass(enabled = true)
  public void beforeClass() {
//    PublicMethed.printAddress(testKeyFordeposit);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
  }

  @Test(enabled = true, description = "Deposit Trc20")
  public void test1DepositTrc20001() {
    PublicMethed.printAddress(testKeyFordeposit);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 11000_000_000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    Account accountAfter = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountAfterBalance = accountAfter.getBalance();
    logger.info("accountAfterBalance:" + accountAfterBalance);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    logger.info("accountSideAfterBalance:" + accountSideAfterBalance);

    long callValue = 1000_000_000L;
    String txid = PublicMethed.triggerContract(mainChainAddressKey, callValue, input,
        maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertEquals(0, infoById.get().getResultValue());
    long fee = infoById.get().getFee();

    Account accountBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();

    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals(10000_000_000L - fee, accountBeforeBalance);
    Assert.assertEquals(callValue, accountSideBeforeBalance);

    String contractName = "trc20Contract";
    String code = Configuration.getByPath("testng.conf")
        .getString("code.code_ContractTRC20");
    String abi = Configuration.getByPath("testng.conf")
        .getString("abi.abi_ContractTRC20");
    String parame = "\"" + Base58.encode58Check(depositAddress) + "\"";

    String deployTxid = PublicMethed
        .deployContractWithConstantParame(contractName, abi, code, "TronToken(address)",
            parame, "",
            maxFeeLimit,
            0L, 100, null, testKeyFordeposit, depositAddress
            , blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(deployTxid, blockingStubFull);
    byte[] trc20Contract = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc20Contract);

    String mapTxid = PublicMethed
        .mappingTrc20(mainChainAddressKey, deployTxid, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(mapTxid, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
    Assert.assertEquals(0, infoById1.get().getResultValue());

    String parame1 = "\"" + Base58.encode58Check(trc20Contract) + "\"";
    byte[] input2 = Hex
        .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame1, false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(sideChainAddressKey, 0, input2,
            maxFeeLimit,
            0, "0",
            depositAddress, testKeyFordeposit, blockingSideStubFull);
    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingSideStubFull);
    logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String tmpAddress = ContractRestule.substring(24);
    logger.info(tmpAddress);
    String addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);

    byte[] sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(sideContractAddress);

    String deposittrx = PublicMethed
        .depositTrc20(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1000, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    logger.info(deposittrx);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    String arg = parame;
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", arg, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0l, input1,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertEquals(1000, ByteArray.toInt(infoById2.get().getContractResult(0).toByteArray()));

    //account not exit

    ECKey ecKey2 = new ECKey(Utils.getRandom());
    byte[] depositAddress1 = ecKey2.getAddress();
    String testKeyFordeposit1 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
    String value = Long.toString(1);

    GrpcAPI.Return response4 = PublicMethed
        .withdrawTrc20ForReturn(ChainIdAddress,
            sideChainAddress, value,
            WalletClient.encode58Check(sideContractAddress),
            maxFeeLimit, depositAddress1, testKeyFordeposit1, blockingStubFull,
            blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response4.getCode());
    Assert.assertEquals(
        "contract validate error : account not exists",
        response4.getMessage().toStringUtf8());

//value=0
//
//        String value1 = Long.toString(0);
//
//        GrpcAPI.Return response5 = PublicMethed.withdrawTrc20ForReturn("TVZfiYbNp9viMQwbnhsyJ7oFevwpFTpPpY",
//                sideChainAddress, value1,
//                WalletClient.encode58Check(sideContractAddress),
//                maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
//        PublicMethed.waitProduceNextBlock(blockingStubFull);
//        PublicMethed.waitProduceNextBlock(blockingSideStubFull);
//        PublicMethed.waitProduceNextBlock(blockingStubFull);
//        PublicMethed.waitProduceNextBlock(blockingSideStubFull);
//
//        Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response5.getCode());
//        Assert.assertEquals(
//                "contract validate error : tokenValue must >= 0",
//                response4.getMessage().toStringUtf8());
  }

  @Test(enabled = true, description = "Deposit Trc20 Error condition with Executor Address before mapping")
  public void test1DepositTrc20002() {
    PublicMethed.printAddress(testKeyFordeposit2);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress2, 11000_000_000L, testDepositAddress, testDepositTrx,
            blockingStubFull));

    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    Account accountAfter = PublicMethed.queryAccount(depositAddress2, blockingStubFull);
    long accountAfterBalance = accountAfter.getBalance();
    logger.info("accountAfterBalance:" + accountAfterBalance);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress2, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    logger.info("accountSideAfterBalance:" + accountSideAfterBalance);

    long callValue = 1000_000_000L;
    String txid = PublicMethed.triggerContract(mainChainAddressKey, callValue, input,
        maxFeeLimit, 0, "", depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertEquals(0, infoById.get().getResultValue());
    long fee = infoById.get().getFee();

    Account accountBefore = PublicMethed.queryAccount(depositAddress2, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress2, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();

    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals(10000_000_000L - fee, accountBeforeBalance);
    Assert.assertEquals(callValue, accountSideBeforeBalance);

    String contractName = "trc20Contract";
    String code = Configuration.getByPath("testng.conf")
        .getString("code.code_ContractTRC20");
    String abi = Configuration.getByPath("testng.conf")
        .getString("abi.abi_ContractTRC20");
    String parame = "\"" + Base58.encode58Check(depositAddress2) + "\"";

    String deployTxid = PublicMethed
        .deployContractWithConstantParame(contractName, abi, code, "TronToken(address)",
            parame, "",
            maxFeeLimit,
            0L, 100, null, testKeyFordeposit2, depositAddress2
            , blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(deployTxid, blockingStubFull);
    byte[] trc20Contract = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc20Contract);

    //deposit trc20 : address not has trc20
    String deposittrx = PublicMethed
        .depositTrc20(WalletClient.encode58Check(trc20Contract), mainChainAddress, 100, 1000000000,
            depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info(deposittrx);

    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(deposittrx, blockingStubFull);
    Optional<Transaction> byId2 = PublicMethed
        .getTransactionById(deposittrx, blockingStubFull);

    Assert.assertEquals(1, infoById2.get().getResultValue());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById2.get().getResMessage().toByteArray()));
    Assert.assertEquals(contractResult.REVERT.getNumber(),
        byId2.get().getRet(0).getContractRet().getNumber());
    Assert.assertEquals(contractResult.REVERT, byId2.get().getRet(0).getContractRet());

    //deposit trc20 : address has trc20
    String deposittrx3 = PublicMethed
        .depositTrc20(WalletClient.encode58Check(trc20Contract), mainChainAddress, 100, 1000000000,
            depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info(deposittrx3);

    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(deposittrx3, blockingStubFull);
    Optional<Transaction> byId3 = PublicMethed
        .getTransactionById(deposittrx3, blockingStubFull);

    Assert.assertEquals(1, infoById3.get().getResultValue());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById3.get().getResMessage().toByteArray()));
    Assert.assertEquals(contractResult.REVERT.getNumber(),
        byId3.get().getRet(0).getContractRet().getNumber());
    Assert.assertEquals(contractResult.REVERT, byId3.get().getRet(0).getContractRet());
  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
