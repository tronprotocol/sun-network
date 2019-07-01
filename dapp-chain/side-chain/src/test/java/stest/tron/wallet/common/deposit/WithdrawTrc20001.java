package stest.tron.wallet.common.deposit;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.spongycastle.util.encoders.Hex;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
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
public class WithdrawTrc20001 {


  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelSolidity = null;
  private static final long now = System.currentTimeMillis();
  private static String tokenName = "testAssetIssue_" + Long.toString(now);
  private static ByteString assetAccountId = null;
  private static final long TotalSupply = 1000L;
  private String description = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetDescription");
  private String url = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetUrl");
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;

  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingSideStubFull = null;


  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;

  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);

  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);

  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);

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

    long callValue = 1000_000_000L;
    String txid = PublicMethed.triggerContract(mainChainAddressKey, callValue, input,
        maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertEquals(0, infoById.get().getResultValue());
    long fee = infoById.get().getFee();

    Account accountBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();

    Assert.assertEquals(10000_000_000L - fee, accountBeforeBalance);
    Assert.assertEquals(callValue, accountSideBeforeBalance);

    String contractName = "trc20Contract";
    String code = Configuration.getByPath("testng.conf")
        .getString("code.code_ContractTRC20");
    String abi = Configuration.getByPath("testng.conf")
        .getString("abi.abi_ContractTRC20");

    String param = "\"" + Base58.encode58Check(depositAddress) + "\"";

    String deployTxid = PublicMethed
        .deployContractWithConstantParame(contractName, abi, code, "TronToken(address)",
            param, "",
            maxFeeLimit,
            0L, 100, null, testKeyFordeposit, depositAddress
            , blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(deployTxid, blockingStubFull);
    byte[] trc20Contract = infoById.get().getContractAddress().toByteArray();
    logger.info("trc20MainContract:" + Base58.encode58Check(trc20Contract));
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc20Contract);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    Optional<Transaction> byId = PublicMethed.getTransactionById(deployTxid, blockingStubFull);
    Assert.assertEquals(byId.get().getRet(0).getContractRet().getNumber(),
        contractResult.SUCCESS_VALUE);
    Assert.assertEquals(byId.get().getRet(0).getContractRetValue(), contractResult.SUCCESS_VALUE);
    Assert.assertEquals(byId.get().getRet(0).getContractRet(), contractResult.SUCCESS);
    Assert.assertEquals(contractResult.SUCCESS, infoById.get().getReceipt().getResult());

    TransactionExtention transactionExtention = PublicMethed
        .triggerContractForExtention(trc20Contract,
            "balanceOf(address)", param, false,
            0, maxFeeLimit, "0", 0, depositAddress, testKeyFordeposit, blockingStubFull);
    Transaction transaction = transactionExtention.getTransaction();
    byte[] result = transactionExtention.getConstantResult(0).toByteArray();
    System.out.println("message:" + transaction.getRet(0).getRet());
    System.out.println(":" + ByteArray
        .toStr(transactionExtention.getResult().getMessage().toByteArray()));
    System.out.println("Result:" + Hex.toHexString(result));

    Assert.assertEquals(100000000000000000L, ByteArray.toLong(ByteArray
        .fromHexString(Hex
            .toHexString(result))));

    HashMap map = PublicMethed
        .mappingTrc20(sideChainAddressKey, mainChainAddressKey, deployTxid, "stest1", "stest1",
            "100", 1000000000,
            depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("map:" + map);

    byte[] sideContractAddress = WalletClient
        .decodeFromBase58Check(map.get("SideContract").toString());

    String deposittrx = PublicMethed
        .depositTrc20(WalletClient.encode58Check(trc20Contract), mainChainAddress,
            100000000000000000L, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    TransactionExtention transactionExtention1 = PublicMethed
        .triggerContractForExtention(trc20Contract,
            "balanceOf(address)", param, false,
            0, maxFeeLimit, "0", 0, depositAddress, testKeyFordeposit, blockingStubFull);
    Transaction transaction1 = transactionExtention1.getTransaction();
    byte[] result1 = transactionExtention1.getConstantResult(0).toByteArray();
    System.out.println("message:" + transaction1.getRet(0).getRet());
    System.out.println(":" + ByteArray
        .toStr(transactionExtention1.getResult().getMessage().toByteArray()));
    System.out.println("Result:" + Hex.toHexString(result1));

    Assert.assertEquals(0, ByteArray.toLong(ByteArray
        .fromHexString(Hex
            .toHexString(result1))));
    String methodStr1 = "balanceOf(address)";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, param, false));

    String txid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            mainChainAddressKey, 0, input1, maxFeeLimit, 0, "0", depositAddress, testKeyFordeposit,
            blockingSideStubFull);
    Optional<TransactionInfo> infoById1;
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingSideStubFull);
    Long returnnumber = ByteArray.toLong(ByteArray.fromHexString(ByteArray.toHexString(
        infoById1.get().getContractResult(0).toByteArray())));
    logger.info("returnnumber:" + returnnumber);
    Assert.assertTrue(100000000000000000L == returnnumber);

    String param2 = "\"" + Base58.encode58Check(trc20Contract) + "\"";

    TransactionExtention transactionExtention2 = PublicMethed
        .triggerContractForExtention(sideChainAddressKey, "mainToSideContractMap(address)",
            param2, false, 0,
            maxFeeLimit, "0", 0, depositAddress, testKeyFordeposit, blockingSideStubFull);
    Transaction transaction2 = transactionExtention2.getTransaction();
    byte[] result2 = transactionExtention2.getConstantResult(0).toByteArray();
    System.out.println("message:" + transaction2.getRet(0).getRet());
    System.out.println(":" + ByteArray
        .toStr(transactionExtention2.getResult().getMessage().toByteArray()));
    System.out.println("Result:" + Hex.toHexString(result2));
    byte[] tmpAddress = new byte[20];
    System.arraycopy(ByteArray.fromHexString(Hex.toHexString(result2)), 12, tmpAddress, 0, 20);
    String addressHex = "41" + ByteArray.toHexString(tmpAddress);
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    Assert.assertEquals(Base58.encode58Check(sideContractAddress),
        addressFinal);

    String txid2 = PublicMethed.withdrawTrc20(Base58.encode58Check(mainChainAddressKey),
        WalletClient.encode58Check(sideContractAddress), "10",
        WalletClient.encode58Check(trc20Contract),
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    logger.info("txid2:" + txid2);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    TransactionExtention transactionExtention3 = PublicMethed
        .triggerContractForExtention(trc20Contract,
            "balanceOf(address)", param, false,
            0, maxFeeLimit, "0", 0, depositAddress, testKeyFordeposit, blockingStubFull);
    Transaction transaction3 = transactionExtention3.getTransaction();
    byte[] result3 = transactionExtention3.getConstantResult(0).toByteArray();
    System.out.println("message:" + transaction3.getRet(0).getRet());
    System.out.println(":" + ByteArray
        .toStr(transactionExtention3.getResult().getMessage().toByteArray()));
    System.out.println("Result:" + Hex.toHexString(result3));

    Assert.assertEquals(10, ByteArray.toLong(ByteArray
        .fromHexString(Hex
            .toHexString(result3))));
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
