package stest.tron.wallet.dailybuild.depositWithdraw;


import static org.tron.api.GrpcAPI.Return.response_code.CONTRACT_VALIDATE_ERROR;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethed;

public class WithdrawTrc20002 {

  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
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
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
  }

  @Test(enabled = true, description = "WithdrawTrc20")
  public void test1WithdrawTrc20001() {

    PublicMethed.printAddress(testKeyFordeposit);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 11000_000_000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    Account accountAfter = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountAfterBalance = accountAfter.getBalance();
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();

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

    String contractName = "trc721Contract";
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
    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String tmpAddress = ContractRestule.substring(24);
    String addressHex = "41" + tmpAddress;
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));

    byte[] sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(sideContractAddress);

    byte[] input1 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", parame, false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return2.getConstantResult(0).toByteArray())));
    Assert.assertTrue(100000000000000000L == mainTrc20Balance);

    String depositTrc20txid = PublicMethed
        .depositTrc20(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1000, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infodeposittrx = PublicMethed
        .getTransactionInfoById(depositTrc20txid, blockingStubFull);
    Assert.assertEquals(0, infodeposittrx.get().getResultValue());

    String sideChainTxid = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0l, input1,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(sideChainTxid, blockingSideStubFull);
    int afterDepositSideChain = ByteArray.toInt(infoById2.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertEquals(1000, afterDepositSideChain);

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance2 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return3.getConstantResult(0).toByteArray())));
    Assert.assertTrue(mainTrc20Balance - 1000 == mainTrc20Balance2);

    String withdrawTrc20Txid = PublicMethed.withdrawTrc20(ChainIdAddress,
        sideChainAddress, "100",
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoByIdwithdrawTrc20 = PublicMethed
        .getTransactionInfoById(withdrawTrc20Txid, blockingSideStubFull);
    Assert.assertEquals(0, infoByIdwithdrawTrc20.get().getResultValue());

    byte[] input4 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", parame, false));
    String sideChainTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0l, input4,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(sideChainTxid1, blockingSideStubFull);
    Assert.assertEquals(0, infoById3.get().getResultValue());
    int afterWithdrawsidechain = ByteArray
        .toInt(infoById3.get().getContractResult(0).toByteArray());
    Assert.assertEquals(afterDepositSideChain - 100, afterWithdrawsidechain);

    TransactionExtention return4 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input4, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long afterWithdrawmainBalance = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return4.getConstantResult(0).toByteArray())));
    Assert.assertTrue(mainTrc20Balance - 900 == afterWithdrawmainBalance);
    Long returnnumber = ByteArray.toLong(ByteArray.fromHexString(ByteArray.toHexString(
        infoById2.get().getContractResult(0).toByteArray())));

    //value>balance
    String value = Long.toString(returnnumber + 1000);
    String txid2 = PublicMethed.withdrawTrc20(ChainIdAddress,
        sideChainAddress, value,
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
//    String txd1 = infoById1.get().getResult().name();
    Optional<TransactionInfo> infoById4 = PublicMethed
        .getTransactionInfoById(txid2, blockingSideStubFull);
    Assert.assertEquals(1, infoById4.get().getResultValue());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById4.get().getResMessage().toByteArray()));

    //account not exit

    ECKey ecKey2 = new ECKey(Utils.getRandom());
    byte[] depositAddress1 = ecKey2.getAddress();
    String testKeyFordeposit1 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());

    String value11 = Long.toString(returnnumber);
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
    String value1 = Long.toString(0);
    String txid3 = PublicMethed.withdrawTrc20(ChainIdAddress,
        sideChainAddress, value1,
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById5 = PublicMethed
        .getTransactionInfoById(txid3, blockingSideStubFull);
//    String txd3 = infoById5.get().getResult().name();
    Assert.assertEquals(1, infoById5.get().getResultValue());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById5.get().getResMessage().toByteArray()));

//        //value=0
//        String value2 = Long.toString(1000);
//        String txid4 = PublicMethed.withdrawTrc20(ChainIdAddress,
//                sideChainAddress, value2,
//                WalletClient.encode58Check(sideContractAddress),
//                maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
//        PublicMethed.waitProduceNextBlock(blockingStubFull);
//        PublicMethed.waitProduceNextBlock(blockingSideStubFull);
//        PublicMethed.waitProduceNextBlock(blockingStubFull);
//        PublicMethed.waitProduceNextBlock(blockingSideStubFull);
//        Optional<TransactionInfo> infoById6;

//        infoById4 = PublicMethed
//                .getTransactionInfoById(txid4, blockingSideStubFull);
//        String  txd4=infoById4.get().getResult().name();
//        Assert.assertEquals(1, infoById4.get().getResultValue());
//        Assert.assertEquals("REVERT opcode executed",
//                ByteArray.toStr(infoById4.get().getResMessage().toByteArray()));

//        //value=Long.Max+1
//
//        String value2 = Long.toString(Long.MAX_VALUE + 1);
//        String txid4 = PublicMethed.withdrawTrc20(ChainIdAddress,
//                sideChainAddress, value2,
//                WalletClient.encode58Check(sideContractAddress),
//                maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
//        byte[] input3= Hex.decode(AbiUtil.parseMethod("balanceOf(address)", arg, false));
//        String ownerTrx1 = PublicMethed
//                .triggerContractSideChain(sideContractAddress, mainChainAddressKey, 0l, input3, 1000000000,
//                        0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
//
//        Assert.assertEquals(1000, ByteArray.toInt(infoById2.get().getContractResult(0).toByteArray()));

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
