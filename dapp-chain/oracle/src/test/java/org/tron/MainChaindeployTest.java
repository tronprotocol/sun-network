package org.tron;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.tron.client.WalletClient;
import org.tron.common.crypto.Hash;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.DataWord;
import org.tron.common.utils.WalletUtil;

@Slf4j
public class MainChaindeployTest {

  private final static WalletClient walletClient = new WalletClient("ip:port",
      ByteArray.fromHexString("privateKey"));
  private final static List<String> oralceLsit = Lists.newArrayList(
      "TVdyt1s88BdiCjKt6K2YuoSmpWScZYK1QF",
      "TCNVmGtkfknHpKSZXepZDXRowHF7kosxcv",
      "TAbzgkG8p3yF5aywKVgq9AaAu6hvF2JrVC",
      "TNNqZuYhMfQvooC4kJwTsMJEQVU3vWGa5u");

  @Test
  public void process() {
    deployGateway();
    deployTrc20();
    deployTrc721();
  }

  @Test
  public void deployGateway() {

    System.out.println("start deploy Gateway ...");
    try {
      String bytecode = "";
      String abi = "";
      String result = exec(
          "./../contract/solc  --allow-paths ../, --bin --abi --optimize ../contract/mainChain/MainChainGateway.sol");
      String[] split = result.split("\n");
      for (int i = 0; i < split.length; ) {
        if (split[i++].matches(".*mainChain/MainChainGateway.sol:MainChainGateway.*")) {
          i++;
          bytecode = split[i++];
          i++;
          abi = split[i++];
        }
      }
      System.out.println("bytecode is " + bytecode);
      System.out.println("abi is " + abi);
      byte[] transactionId = walletClient.deployContract("gateway", abi, bytecode);
      System.out.println("txid is " + ByteArray.toHexString(transactionId));
      byte[] contractAddress = generateContractAddress(transactionId);
      String address = WalletUtil.encode58Check(contractAddress);
      System.out.println("main chain gateway : " + address);
      System.out.println("chain.id is : " + ByteArray.toHexString(contractAddress));
      Thread.sleep(5000L);
      oralceLsit.forEach(oralce -> {
        walletClient
            .triggerSideContract(address, "addOracle(address)", String.format("\"%s\"", oralce));
      });

      Thread.sleep(3000L);
      byte[] ret = walletClient
          .triggerConstantContract(address, "numOracles()", "");
      Assert.assertEquals(AbiUtil.unpackUint(ret), 4);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void deployTrc20() {
    System.out.println("start deploy trc20 ...");
    try {
      String bytecode = "";
      String abi = "";
      String result = exec(
          "./../contract/solc  --allow-paths ../, --bin --abi --optimize ../contract/sideChain/DAppTRC20.sol");
      String[] split = result.split("\n");
      for (int i = 0; i < split.length; ) {
        if (split[i++].matches(".*sideChain/DAppTRC20.sol:DAppTRC20.*")) {
          i++;
          bytecode = split[i++];
          i++;
          abi = split[i++];
        }
      }
      String paramter = ByteArray.toHexString(
          AbiUtil
              .encodeInput("constructor(address,string,string,uint8)",
                  "\"" + WalletUtil.encode58Check(walletClient.getAddress())
                      + "\",\"nmb\",\"_nmb\",2"));
      System.out.println("bytecode is " + bytecode);
      System.out.println("abi is " + abi);
      byte[] transactionId = walletClient.deployContract("gateway", abi, bytecode + paramter);
      byte[] contractAddress = generateContractAddress(transactionId);
      String address = WalletUtil.encode58Check(contractAddress);
      System.out.println("main chain trc20 address is : " + address);
      System.out.println("mapping trc20 " + ByteArray.toHexString(transactionId) + " 1000000000");
      Thread.sleep(3000L);
      walletClient
          .triggerContract(address, "mint(address,uint256)",
              "\"TNNqZuYhMfQvooC4kJwTsMJEQVU3vWGa5u\",1000000000000");
      Thread.sleep(3000L);
      byte[] ret = walletClient
          .triggerConstantContract(address, "balanceOf(address)",
              "\"TNNqZuYhMfQvooC4kJwTsMJEQVU3vWGa5u\"");

      Assert.assertEquals(AbiUtil.unpackUint(ret), 1000000000000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void deployTrc721() {
    System.out.println("start deploy trc721 ...");
    try {
      String bytecode = "";
      String abi = "";
      String result = exec(
          "./../contract/solc  --allow-paths ../, --bin --abi --optimize ../contract/sideChain/DAppTRC721.sol");
      String[] split = result.split("\n");
      for (int i = 0; i < split.length; ) {
        if (split[i++].matches(".*sideChain/DAppTRC721.sol:DAppTRC721.*")) {
          i++;
          bytecode = split[i++];
          i++;
          abi = split[i++];
        }
      }
      String paramter = ByteArray.toHexString(
          AbiUtil
              .encodeInput("constructor(address,string,string,uint8)",
                  "\"" + WalletUtil.encode58Check(walletClient.getAddress())
                      + "\",\"nmb\",\"_nmb\",2"));
      System.out.println("bytecode is " + bytecode);
      System.out.println("abi is " + abi);
      byte[] transactionId = walletClient.deployContract("gateway", abi, bytecode + paramter);
      byte[] contractAddress = generateContractAddress(transactionId);
      String address = WalletUtil.encode58Check(contractAddress);
      System.out.println("main chain trc721 address is : " + address);
      System.out.println("mapping trc721 " + ByteArray.toHexString(transactionId) + " 1000000000");
      Thread.sleep(3000L);
      walletClient
          .triggerContract(address, "mint(address,uint256)",
              "\"TNNqZuYhMfQvooC4kJwTsMJEQVU3vWGa5u\",1234567890987654321");
      Thread.sleep(3000L);
      byte[] ret = walletClient
          .triggerConstantContract(address, "ownerOf(uint256)",
              "1234567890987654321");

      Assert.assertEquals(WalletUtil.encode58CheckWithoutPrefix(new DataWord(ret).getLast20Bytes()),
          WalletUtil.encode58Check(walletClient.getAddress()));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static String exec(String command) throws InterruptedException {
    String returnString = "";
    Process pro = null;
    Runtime runTime = Runtime.getRuntime();
    if (runTime == null) {
      logger.error("Create runtime false!");
    }
    try {
      pro = runTime.exec(command);
      BufferedReader input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
      PrintWriter output = new PrintWriter(new OutputStreamWriter(pro.getOutputStream()));
      String line;
      while ((line = input.readLine()) != null) {
        returnString = returnString + line + "\n";
      }
      input.close();
      output.close();
      pro.destroy();
    } catch (IOException ex) {
      logger.error(null, ex);
    }
    return returnString;
  }

  @Test
  public void test1() {
    System.out.println(ByteArray.toHexString(
        AbiUtil
            .parseMethod("addOracle(address)", "\"TRxETQim3Jn5TYqLeAnpyF5XdQeg7NUcSJ\"", false)));
  }

  public byte[] generateContractAddress(byte[] txRawDataHash) {

    // get owner address
    // this address should be as same as the onweraddress in trx, DONNOT modify it
    byte[] ownerAddress = walletClient.getAddress();

    // combine
    byte[] combined = new byte[txRawDataHash.length + ownerAddress.length];
    System.arraycopy(txRawDataHash, 0, combined, 0, txRawDataHash.length);
    System.arraycopy(ownerAddress, 0, combined, txRawDataHash.length, ownerAddress.length);

    return Hash.sha3omit12(combined);

  }
}