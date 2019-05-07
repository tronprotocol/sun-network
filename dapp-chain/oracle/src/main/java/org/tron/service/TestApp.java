package org.tron.service;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.tron.common.utils.ByteArray;
import org.tron.db.TransactionExtentionStore;
import org.tron.service.check.TransactionExtention;

public class TestApp {

  // public static void main(String[] args) {
  //   for (int i = 0; i < 100; i++) {
  //
  //     new Thread(() -> {
  //       DoTask doTask = new DoTask();
  //       doTask.run("11111");
  //     }).start();
  //     new Thread(() -> {
  //       DoTask doTask = new DoTask();
  //       doTask.run("222222");
  //     }).start();
  //     new Thread(() -> {
  //       DoTask doTask = new DoTask();
  //       doTask.run("3333333");
  //     }).start();
  //     new Thread(() -> {
  //       DoTask doTask = new DoTask();
  //       doTask.run("44444444");
  //     }).start();
  //     new Thread(() -> {
  //       DoTask doTask = new DoTask();
  //       doTask.run("555555555");
  //     }).start();
  //     new Thread(() -> {
  //       DoTask doTask = new DoTask();
  //       doTask.run("6666666666");
  //     }).start();
  //     try {
  //       Thread.sleep(50);
  //     } catch (InterruptedException e) {
  //       e.printStackTrace();
  //     }
  //   }
  // }
  public static void main(String[] args) {
    TransactionExtentionStore store = TransactionExtentionStore.getInstance();
    store.initDB();
    store.putData(ByteArray.fromString("aa"), ByteArray.fromString("pppp"));
    System.out.println(ByteArray.toStr(store.getData(ByteArray.fromString("aa"))));
  }
}

class DoTask {

  public void run(String s) {
    TransactionExtention transactionId = null;
    CheckTrans.startCheck(transactionId);
  }
}

class CheckTrans {

  private static final ScheduledExecutorService syncExecutor = Executors
      .newScheduledThreadPool(100);

  public static void startCheck(TransactionExtention trxId) {
    System.out.println("add schedule");
    syncExecutor
        .schedule(
            () -> CheckTrans.checkTransactionId(trxId), 1000,
            TimeUnit.MILLISECONDS);
  }

  private static void checkTransactionId(TransactionExtention trxId) {
    Random random = new Random();
    int i = random.nextInt(10);
    if (i > 4) {
      System.out.println("failed " + i);
      CheckTrans.startCheck(trxId);
    } else {
      System.out.println("success " + i + " " + trxId.getTransactionId());
    }

  }
}