package stest.tron.wallet.common.deploy;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class depositTokenFoundationForDailybuild {

  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] ChainIdAddressKey = WalletClient.decodeFromBase58Check(ChainIdAddress);
  final String tokenFoundationKey001 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.sideTokenOwnerKey");
  final byte[] tokenFoundationAddress001 = PublicMethed.getFinalAddress(tokenFoundationKey001);
  final String tokenFoundationId001 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.sideTokenId");
  final String tokenFoundationKey002 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.sideTokenOwnerKey2");
  final byte[] tokenFoundationAddress002 = PublicMethed.getFinalAddress(tokenFoundationKey002);
  final String tokenFoundationId002 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.sideTokenId2");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  ByteString assetAccountId;
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
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);

    String trc20MainAddress1 = "";
    String trc20SideAddress1 = "";
    String trc20MainAddress2 = "";
    String trc20SideAddress2 = "";
    String trc721MainAddress1 = "";
    String trc721SideAddress1 = "";
    String trc721MainAddress2 = "";
    String trc721SideAddress2 = "";


    // deposit TRX
    {
      for (int i=0; i< 3; i++){
        Account sideAccount = PublicMethed.queryAccount(tokenFoundationAddress001,
            blockingSideStubFull);
        Long sideBalance = sideAccount.getBalance();

        String methodStr = "depositTRX()";
        byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

        Long callValue = 1000000000_000_000L;
        String txid = PublicMethed
            .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
                callValue, input, maxFeeLimit, 0, "", tokenFoundationAddress001,
                tokenFoundationKey001, blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingSideStubFull);
        PublicMethed.waitProduceNextBlock(blockingSideStubFull);
        PublicMethed.waitProduceNextBlock(blockingSideStubFull);

        Optional<TransactionInfo> infoById = PublicMethed
            .getTransactionInfoById(txid, blockingStubFull);
        Assert.assertTrue(infoById.get().getResultValue() == 0);

        sideAccount = PublicMethed.queryAccount(tokenFoundationAddress001,
            blockingSideStubFull);
        if(sideBalance + callValue >= sideAccount.getBalance()) {
          break;
        }else if(i == 3 ){
          logger.info("Deposit TRX failed");
        }
      }

      for (int i=0; i< 3; i++){
        Account sideAccount = PublicMethed.queryAccount(tokenFoundationAddress002,
            blockingSideStubFull);
        Long sideBalance = sideAccount.getBalance();

        String methodStr = "depositTRX()";
        byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

        Long callValue = 1000000000_000_000L;
        String txid = PublicMethed
            .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
                callValue, input, maxFeeLimit, 0, "", tokenFoundationAddress002,
                tokenFoundationKey002, blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingSideStubFull);
        PublicMethed.waitProduceNextBlock(blockingSideStubFull);
        PublicMethed.waitProduceNextBlock(blockingSideStubFull);

        Optional<TransactionInfo> infoById = PublicMethed
            .getTransactionInfoById(txid, blockingStubFull);
        Assert.assertTrue(infoById.get().getResultValue() == 0);

        sideAccount = PublicMethed.queryAccount(tokenFoundationAddress002,
            blockingSideStubFull);
        if(sideBalance + callValue >= sideAccount.getBalance()) {
          break;
        }else if(i == 3 ){
          logger.info("Deposit TRX failed");
        }
      }

    }

    //  deposit TRC10
    {
      String methodStr = "depositTRC10(uint64,uint64)";
      long inputTokenValue = 1000000000000L;
      for (int tryConut = 0;tryConut < 3; tryConut++) {
        // depositTRC10 for tokenFoundationAccount001
        String inputParam = tokenFoundationId001 + "," + inputTokenValue;
        byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));
        String txid = PublicMethed
            .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
                0,
                input,
                maxFeeLimit, inputTokenValue, tokenFoundationId001, tokenFoundationAddress001,
                tokenFoundationKey001,
                blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingSideStubFull);
        PublicMethed.waitProduceNextBlock(blockingSideStubFull);
        PublicMethed.waitProduceNextBlock(blockingSideStubFull);
        Account FoundationAccount = PublicMethed
            .queryAccount(tokenFoundationKey001, blockingSideStubFull);
        try {
          String tokenid = FoundationAccount.getAssetV2Map().keySet().toArray()[0].toString();
          Long tokenValue = FoundationAccount.getAssetV2Map().get(tokenid);

          logger.info("FoundationAccount.getAssetIssuedID().toStringUtf8(): " + tokenid);
          logger.info("FoundationAccount.getAssetCount(): " + tokenValue);

          if (tokenValue != null && tokenValue >= inputTokenValue) {
            break;
          } else if (tryConut > 3) {
            logger.info("deposit Token 002 FAIED!!");
            Assert.assertTrue(false);
          }
        } catch (Exception e) {
          if (tryConut > 3) {
            logger.info("deposit Token 002 FAIED!!");
            Assert.assertTrue(false);
          }
        }
      }

      for (int tryConut = 0;tryConut < 3; tryConut++) {
        // depositTRC10 for tokenFoundationAccount002
        String inputParam2 = tokenFoundationId002 + "," + inputTokenValue;
        byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam2, false));
        String txid2 = PublicMethed
            .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
                0,
                input2,
                maxFeeLimit, inputTokenValue, tokenFoundationId002, tokenFoundationAddress002,
                tokenFoundationKey002,
                blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingSideStubFull);

        Account FoundationAccount = PublicMethed
            .queryAccount(tokenFoundationKey002, blockingSideStubFull);
        try {
          String tokenid = FoundationAccount.getAssetV2Map().keySet().toArray()[0].toString();
          Long tokenValue = FoundationAccount.getAssetV2Map().get(tokenid);

          logger.info("FoundationAccount.getAssetIssuedID().toStringUtf8(): " + tokenid);
          logger.info("FoundationAccount.getAssetCount(): " + tokenValue);

          if (tokenValue != null && tokenValue >= inputTokenValue) {
            break;
          } else if (tryConut > 3) {
            logger.info("deposit Token 002 FAIED!!");
            Assert.assertTrue(false);
          }
        } catch (Exception e) {
          if (tryConut > 3) {
            logger.info("deposit Token 002 FAIED!!");
            Assert.assertTrue(false);
          }
        }
      }
    }

    //  deposit TRC20
    {
      String contractName = "trc20Contract";
      String code = Configuration.getByPath("testng.conf")
          .getString("code.code_ContractTRC20");
      String abi = Configuration.getByPath("testng.conf")
          .getString("abi.abi_ContractTRC20");

      {

        String parame = "\"" + Base58.encode58Check(tokenFoundationAddress001) + "\"";
        logger.info("parame: " + parame);

        String deployTxid = PublicMethed
            .deployContractWithConstantParame(contractName, abi, code, "TronToken(address)",
                parame, "",
                maxFeeLimit,
                0L, 100, null, tokenFoundationKey001, tokenFoundationAddress001
                , blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);

        Optional<TransactionInfo> infoById = PublicMethed
            .getTransactionInfoById(deployTxid, blockingStubFull);
        byte[] trc20Contract = infoById.get().getContractAddress().toByteArray();
        Assert.assertEquals(0, infoById.get().getResultValue());
        Assert.assertNotNull(trc20Contract);
        trc20MainAddress1 = WalletClient.encode58Check(trc20Contract);

        String mapTxid = PublicMethed
            .mappingTrc20(WalletClient.decodeFromBase58Check(mainGateWayAddress), deployTxid, 1000000000,
                tokenFoundationAddress001, tokenFoundationKey001, blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);

        Optional<TransactionInfo> infoById1 = PublicMethed
            .getTransactionInfoById(mapTxid, blockingSideStubFull);
        Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
        Assert.assertEquals(0, infoById1.get().getResultValue());


        String parame1 = "\"" + Base58.encode58Check(trc20Contract) + "\"";
        byte[] input1 = Hex
            .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame1, false));
        TransactionExtention return1 = PublicMethed
            .triggerContractForTransactionExtention(WalletClient.decodeFromBase58Check(sideGatewayAddress), 0, input1,
                maxFeeLimit,
                0, "0",
                tokenFoundationAddress001, tokenFoundationKey001, blockingSideStubFull);
        logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
        String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

        for(int n=0; n < 60; n++){
          if(ContractRestule.equals("0000000000000000000000000000000000000000000000000000000000000000")){
            try {
              Thread.sleep(1000);
            }catch (Exception e){
              continue;
            }
            return1 = PublicMethed
                .triggerContractForTransactionExtention(WalletClient.decodeFromBase58Check(sideGatewayAddress), 0, input1,
                    maxFeeLimit,
                    0, "0",
                    tokenFoundationAddress001, tokenFoundationKey001, blockingSideStubFull);
            logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
            ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

          }else {
            break;
          }
        }

        String tmpAddress = ContractRestule.substring(24);
        logger.info(tmpAddress);
        String addressHex = "41" + tmpAddress;
        logger.info("address_hex: " + addressHex);
        String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
        logger.info("address_final: " + addressFinal);

        byte[] sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
        Assert.assertNotNull(sideContractAddress);

        String depositTrc20Id = PublicMethed
            .depositTrc20(WalletClient.encode58Check(trc20Contract), mainGateWayAddress,
                50000000000000000L,
                1000000000,
                tokenFoundationAddress001, tokenFoundationKey001, blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);

        Optional<TransactionInfo> infoById4 = PublicMethed
            .getTransactionInfoById(depositTrc20Id, blockingSideStubFull);
        Assert.assertEquals(0, infoById4.get().getResultValue());

        trc20SideAddress1 = addressFinal;
        for(int n=0; n < 60; n++){
          TransactionExtention return2 = PublicMethed
              .triggerConstantContractForExtention(sideContractAddress,"balanceOf(address)",
                  parame, false, 0l, 1000000000,
                  "0", 0l, tokenFoundationAddress001, tokenFoundationKey001, blockingSideStubFull);

          if(return2.getConstantResultCount() == 0 || ByteArray.toLong(ByteArray
              .fromHexString(ByteArray.toHexString(return2.getConstantResult(0).toByteArray())))==0){
            try {
                Thread.sleep(1000);
            }catch (Exception e){
              continue;
            }
          }else {
            logger.info("balanceOf(address) : " + ByteArray.toLong(ByteArray
                .fromHexString(ByteArray.toHexString(return2.getConstantResult(0).toByteArray()))));
            break;
          }
        }


      }


      {
        String parame = "\"" + Base58.encode58Check(tokenFoundationAddress002) + "\"";

        String deployTxid = PublicMethed
            .deployContractWithConstantParame(contractName, abi, code, "TronToken(address)",
                parame, "",
                maxFeeLimit,
                0L, 100, null, tokenFoundationKey002, tokenFoundationAddress002
                , blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);

        Optional<TransactionInfo> infoById = PublicMethed
            .getTransactionInfoById(deployTxid, blockingStubFull);
        byte[] trc20Contract = infoById.get().getContractAddress().toByteArray();
        Assert.assertEquals(0, infoById.get().getResultValue());
        Assert.assertNotNull(trc20Contract);
        trc20MainAddress2 = WalletClient.encode58Check(trc20Contract);

        String mapTxid = PublicMethed
            .mappingTrc20(WalletClient.decodeFromBase58Check(mainGateWayAddress), deployTxid, 1000000000,
                tokenFoundationAddress002, tokenFoundationKey002, blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);

        Optional<TransactionInfo> infoById1 = PublicMethed
            .getTransactionInfoById(mapTxid, blockingSideStubFull);
        Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
        Assert.assertEquals(0, infoById1.get().getResultValue());


        String parame1 = "\"" + Base58.encode58Check(trc20Contract) + "\"";
        byte[] input1 = Hex
            .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame1, false));
        TransactionExtention return1 = PublicMethed
            .triggerContractForTransactionExtention(WalletClient.decodeFromBase58Check(sideGatewayAddress), 0, input1,
                maxFeeLimit,
                0, "0",
                tokenFoundationAddress002, tokenFoundationKey002, blockingSideStubFull);
        logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
        String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

        for(int n=0; n < 60; n++){

          return1 = PublicMethed
              .triggerContractForTransactionExtention(WalletClient.decodeFromBase58Check(sideGatewayAddress), 0, input1,
                  maxFeeLimit,
                  0, "0",
                  tokenFoundationAddress002, tokenFoundationKey002, blockingSideStubFull);
          logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
          ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

          if(ContractRestule.equals("0000000000000000000000000000000000000000000000000000000000000000")){
            try {
              Thread.sleep(1000);
            }catch (Exception e){
              continue;
            }
          }else {break;}

        }

        String tmpAddress = ContractRestule.substring(24);
        logger.info(tmpAddress);
        String addressHex = "41" + tmpAddress;
        logger.info("address_hex: " + addressHex);
        String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
        logger.info("address_final: " + addressFinal);

        byte[] sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
        Assert.assertNotNull(sideContractAddress);

        String depositTrc20Id = PublicMethed
            .depositTrc20(WalletClient.encode58Check(trc20Contract), mainGateWayAddress, 50000000000000000L, 1000000000,
                tokenFoundationAddress002, tokenFoundationKey002, blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);

        Optional<TransactionInfo> infoById4 = PublicMethed
            .getTransactionInfoById(depositTrc20Id, blockingStubFull);
        Assert.assertEquals(0, infoById4.get().getResultValue());
        trc20SideAddress2 = addressFinal;

        for(int n=0; n < 60; n++){
          TransactionExtention return2 = PublicMethed
              .triggerConstantContractForExtention(sideContractAddress,"balanceOf(address)",
                  parame, false, 0l, 1000000000,
                  "0", 0l, tokenFoundationAddress002, tokenFoundationKey002, blockingSideStubFull);

          if(return2.getConstantResultCount() == 0 || ByteArray.toLong(ByteArray
              .fromHexString(ByteArray.toHexString(return2.getConstantResult(0).toByteArray())))==0){
            try {
              Thread.sleep(1000);
            }catch (Exception e){
              continue;
            }
          }else {
            logger.info("balanceOf(address) : " + ByteArray.toLong(ByteArray
                .fromHexString(ByteArray.toHexString(return2.getConstantResult(0).toByteArray()))));
            break;
          }
        }
      }


    }

    //  deposit TRC721
    {
      String contractName = "trc721";
      String code = Configuration.getByPath("testng.conf")
          .getString("code.code_ContractTRC721");
      String abi = Configuration.getByPath("testng.conf")
          .getString("abi.abi_ContractTRC721");

      {
        String parame = "\"" + Base58.encode58Check(tokenFoundationAddress001) + "\","
            + "\"tokenFoundation001"
            + "\","
            + "\"tokenFoundation001\"";

        String deployTxid = PublicMethed
            .deployContractWithConstantParame(contractName, abi, code,
                "constructor(address,string,string)",
                parame, "", maxFeeLimit,
                0L, 100, null, tokenFoundationKey001, tokenFoundationAddress001
                , blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);

        Optional<TransactionInfo> infoById = PublicMethed
            .getTransactionInfoById(deployTxid, blockingStubFull);
        byte[] trc721Contract = infoById.get().getContractAddress().toByteArray();
        Assert.assertEquals(0, infoById.get().getResultValue());
        Assert.assertNotNull(trc721Contract);
        trc721MainAddress1 = WalletClient.encode58Check(trc721Contract);

        String parame1 = "\"" + Base58.encode58Check(tokenFoundationAddress001) + "\"," + 1001;
        String mintTxid = PublicMethed
            .triggerContract(trc721Contract, "mint(address,uint256)", parame1, false, 0, maxFeeLimit,
                tokenFoundationAddress001, tokenFoundationKey001, blockingStubFull);
        infoById = PublicMethed.getTransactionInfoById(mintTxid, blockingStubFull);
        Assert.assertNotNull(mintTxid);
        Assert.assertEquals(0, infoById.get().getResultValue());
        Assert.assertEquals("SUCESS", infoById.get().getResult().name());

        String mapTxid = PublicMethed
            .mappingTrc721(WalletClient.decodeFromBase58Check(mainGateWayAddress), deployTxid,
                1000000000,
                tokenFoundationAddress001, tokenFoundationKey001, blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingSideStubFull);

        Optional<TransactionInfo> infoById1 = PublicMethed
            .getTransactionInfoById(mapTxid, blockingSideStubFull);
        Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
        Assert.assertEquals(0, infoById1.get().getResultValue());
        Assert.assertNotNull(mapTxid);

        String parame2 = "\"" + Base58.encode58Check(trc721Contract) + "\"";
        byte[] input2 = Hex
            .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame2, false));
        TransactionExtention return1 = PublicMethed
            .triggerContractForTransactionExtention(WalletClient.decodeFromBase58Check(sideGatewayAddress),
                0, input2, maxFeeLimit, 0, "0",
                tokenFoundationAddress001, tokenFoundationKey001, blockingSideStubFull);
        logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
        String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

        for(int n=0; n < 60; n++){
          if(ContractRestule.equals("0000000000000000000000000000000000000000000000000000000000000000")){
            try {
              Thread.sleep(1000);
            }catch (Exception e){
              continue;
            }
            return1 = PublicMethed
                .triggerContractForTransactionExtention(WalletClient.decodeFromBase58Check(sideGatewayAddress), 0, input2,
                    maxFeeLimit,
                    0, "0",
                    tokenFoundationAddress001, tokenFoundationKey001, blockingSideStubFull);
            logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
            ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

          }
        }

        String tmpAddress = ContractRestule.substring(24);
        logger.info(tmpAddress);
        String addressHex = "41" + tmpAddress;
        logger.info("address_hex: " + addressHex);
        String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
        logger.info("address_final: " + addressFinal);

        byte[] sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
        Assert.assertEquals(0, infoById.get().getResultValue());
        Assert.assertNotNull(sideContractAddress);
        trc721SideAddress1 = addressFinal;

      }

      {
        String parame = "\"" + Base58.encode58Check(tokenFoundationAddress002) + "\","
            + "\"tokenFoundation002"
            + "\",\"tokenFoundation002\"";

        String deployTxid = PublicMethed
            .deployContractWithConstantParame(contractName, abi, code,
                "constructor(address,string,string)",
                parame, "", maxFeeLimit,
                0L, 100, null, tokenFoundationKey002, tokenFoundationAddress002
                , blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);

        Optional<TransactionInfo> infoById = PublicMethed
            .getTransactionInfoById(deployTxid, blockingStubFull);
        byte[] trc721Contract = infoById.get().getContractAddress().toByteArray();
        Assert.assertEquals(0, infoById.get().getResultValue());
        Assert.assertNotNull(trc721Contract);
        trc721MainAddress2 = WalletClient.encode58Check(trc721Contract);

        String parame1 = "\"" + Base58.encode58Check(tokenFoundationAddress002) + "\"," + 1001;
        String mintTxid = PublicMethed
            .triggerContract(trc721Contract, "mint(address,uint256)", parame1, false, 0, maxFeeLimit,
                tokenFoundationAddress002, tokenFoundationKey002, blockingStubFull);
        infoById = PublicMethed.getTransactionInfoById(mintTxid, blockingStubFull);
        Assert.assertNotNull(mintTxid);
        Assert.assertEquals(0, infoById.get().getResultValue());
        Assert.assertEquals("SUCESS", infoById.get().getResult().name());

        String mapTxid = PublicMethed
            .mappingTrc721(WalletClient.decodeFromBase58Check(mainGateWayAddress), deployTxid,
                1000000000,
                tokenFoundationAddress002, tokenFoundationKey002, blockingStubFull);
        PublicMethed.waitProduceNextBlock(blockingStubFull);

        Optional<TransactionInfo> infoById1 = PublicMethed
            .getTransactionInfoById(mapTxid, blockingSideStubFull);
        Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
        Assert.assertEquals(0, infoById1.get().getResultValue());
        Assert.assertNotNull(mapTxid);

        String parame2 = "\"" + Base58.encode58Check(trc721Contract) + "\"";
        byte[] input2 = Hex
            .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame2, false));
        TransactionExtention return1 = PublicMethed
            .triggerContractForTransactionExtention(WalletClient.decodeFromBase58Check(sideGatewayAddress),
                0, input2, maxFeeLimit, 0, "0",
                tokenFoundationAddress002, tokenFoundationKey002, blockingSideStubFull);
        logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
        String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

        for(int n=0; n < 60; n++){
          if(ContractRestule.equals("0000000000000000000000000000000000000000000000000000000000000000")){
            try {
              Thread.sleep(1000);
            }catch (Exception e){
              continue;
            }
            return1 = PublicMethed
                .triggerContractForTransactionExtention(WalletClient.decodeFromBase58Check(sideGatewayAddress), 0, input2,
                    maxFeeLimit,
                    0, "0",
                    tokenFoundationAddress002, tokenFoundationKey002, blockingSideStubFull);
            logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
            ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

          }
        }

        String tmpAddress = ContractRestule.substring(24);
        logger.info(tmpAddress);
        String addressHex = "41" + tmpAddress;
        logger.info("address_hex: " + addressHex);
        String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
        logger.info("address_final: " + addressFinal);

        byte[] sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
        Assert.assertEquals(0, infoById.get().getResultValue());
        Assert.assertNotNull(sideContractAddress);
        trc721SideAddress2 = addressFinal;
      }

    }


    String outputPath = "./src/test/resources/TokenFoundationAddress";
    try {
      File mainChainFile = new File(outputPath);
      Boolean cun = mainChainFile.createNewFile();
      FileWriter writer = new FileWriter(mainChainFile);
      BufferedWriter out = new BufferedWriter(writer);
      out.write("trc20MainAddress1=" + trc20MainAddress1 + "\n");
      out.write("trc20SideAddress1=" + trc20SideAddress1 + "\n");
      out.write("trc20MainAddress2=" + trc20MainAddress2 + "\n");
      out.write("trc20SideAddress2=" + trc20SideAddress2 + "\n");
      out.write("trc721MainAddress1=" + trc721MainAddress1 + "\n");
      out.write("trc721SideAddress1=" + trc721SideAddress1 + "\n");
      out.write("trc721MainAddress2=" + trc721MainAddress2 + "\n");
      out.write("trc721SideAddress2=" + trc721SideAddress2 + "\n");

      out.close();
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  @Test(enabled = true, description = "depositTokenFoundation")
  public void depositTokenFoundationTest() {

  }
}
