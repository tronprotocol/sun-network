package org.tron.sunapi;

import java.nio.charset.Charset;
import org.junit.Assert;
import org.junit.Test;
import org.tron.protos.Protocol.Account;
import org.tron.sunapi.response.TransactionResponse;

public class SunNetworkTest {

  public static String priKey = "e901ef62b241b6f1577fd6ea34ef8b1c4b3ddee1e3c051b9e63f5ff729ad47a1";

  public static SunNetwork sdk = new SunNetwork();
  {
    sdk.init("config.conf", priKey);
  }


  @Test
  public void depositTrxTest() {

    long balanceMain1 = 0;
    long balanceMain2 = 0;
    long balanceSide1 = 0;
    long balanceSide2 = 0;

    System.out.println("\r\n===================== balance before deposit ========================");
    {
      SunNetworkResponse<Long> resp = sdk.getMainChainService().getBalance();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        balanceMain1 = resp.getData();
        System.out.println("main chain balance is:" + resp.getData());
      }

      resp = sdk.getSideChainService().getBalance();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        balanceSide1 = resp.getData();
        System.out.println("side chain balance is:" + resp.getData());
      }
    }

    System.out.println("\r\n===================== deposit 124 trx ===============================");
    {
      SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService()
          .depositTrx("TTGhuSDKr561gzHFjkZ1V4ZtMgUEFLa7ct", 124, 1000000);

      System.out.println("Error code desc: " + resp.getDesc());
      System.out.println("transaction result: " + resp.getData().getResult());
      System.out.println("transaction id: " + resp.getData().getTrxId());
    }

    System.out.println("\r\n===================== balance after deposit, sleep 6s ===============");
    {
      try {
        Thread.sleep(6000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      SunNetworkResponse<Long> resp = sdk.getMainChainService().getBalance();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        balanceMain2 = resp.getData();
        System.out.println("main chain balance is:" + resp.getData());
      }

      Assert.assertEquals(balanceMain1, balanceMain2 + 124);

      resp = sdk.getSideChainService().getBalance();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        balanceSide2 = resp.getData();
        System.out.println("side chain balance is:" + resp.getData());
      }

      Assert.assertEquals(balanceSide2, balanceSide1 + 124);
    }
  }

  @Test
  public void getAddressTest() {
    SunNetworkResponse<String> resp = sdk.getMainChainService().getAddress();

    Assert.assertEquals(resp.getData(), "TVdyt1s88BdiCjKt6K2YuoSmpWScZYK1QF");
  }

  @Test
  public void getAccountTest() {
    long balanceMain1 = -1;

    SunNetworkResponse<Long> resp = sdk.getMainChainService().getBalance();
    if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
      balanceMain1 = resp.getData();
    }

    SunNetworkResponse<Account> resp2 = sdk.getMainChainService().getAccount("TVdyt1s88BdiCjKt6K2YuoSmpWScZYK1QF");
    Assert.assertEquals(resp2.getData().getBalance(), balanceMain1);
  }

  @Test
  public void accountIdTest() {
    Account account1;
    Account account2;
    String accountId = "accountId_test";

    SunNetworkResponse<Account> resp1 = sdk.getMainChainService().getAccount("TVdyt1s88BdiCjKt6K2YuoSmpWScZYK1QF");
    Assert.assertEquals(resp1.getCode(), "0");
    account1 = resp1.getData();
    if(!account1.getAccountId().isEmpty()) {
      accountId = new String(account1.getAccountId().toByteArray(), Charset.forName("UTF-8"));
    } else {
      SunNetworkResponse<Integer> resp0 = sdk.getMainChainService().setAccountId(accountId);
      Assert.assertEquals(resp0.getCode(), "0");

      resp1 = sdk.getMainChainService().getAccount("TVdyt1s88BdiCjKt6K2YuoSmpWScZYK1QF");
      Assert.assertEquals(resp1.getCode(), "0");
    }

    String id = new String(account1.getAccountId().toByteArray(), Charset.forName("UTF-8"));
    Assert.assertEquals(accountId, id);

    SunNetworkResponse<Account> resp2 = sdk.getMainChainService().getAccountById(accountId);
    Assert.assertEquals(resp1.getCode(), "0");
    account2 = resp2.getData();

    Assert.assertEquals(account1.getBalance(), account2.getBalance());
    Assert.assertEquals(account1.getAccountId(), account2.getAccountId());
  }



}
