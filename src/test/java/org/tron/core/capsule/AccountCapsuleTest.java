package org.tron.core.capsule;

import com.google.protobuf.ByteString;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.tron.common.application.TronApplicationContext;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.FileUtil;
import org.tron.core.Constant;
import org.tron.core.Wallet;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.db.Manager;
import org.tron.protos.Contract;
import org.tron.protos.Protocol.AccountType;
import org.tron.protos.Protocol.Key;
import org.tron.protos.Protocol.Permission;
import org.tron.protos.Protocol.Vote;

@Ignore
public class AccountCapsuleTest {

  private static final String dbPath = "output_accountCapsule_test";
  private static final Manager dbManager;
  private static final TronApplicationContext context;
  private static final String OWNER_ADDRESS;
  private static final String ASSET_NAME = "trx";
  private static final long TOTAL_SUPPLY = 10000L;
  private static final int TRX_NUM = 10;
  private static final int NUM = 1;
  private static final long START_TIME = 1;
  private static final long END_TIME = 2;
  private static final int VOTE_SCORE = 2;
  private static final String DESCRIPTION = "TRX";
  private static final String URL = "https://tron.network";


  static AccountCapsule accountCapsuleTest;
  static AccountCapsule accountCapsule;

  static {
    Args.setParam(new String[]{"-d", dbPath, "-w"}, Constant.TEST_CONF);
    context = new TronApplicationContext(DefaultConfig.class);
    dbManager = context.getBean(Manager.class);

    OWNER_ADDRESS = Wallet.getAddressPreFixString() + "a06a17a49648a8ad32055c06f60fa14ae46df91234";
  }


  @BeforeClass
  public static void init() {
    ByteString accountName = ByteString.copyFrom(AccountCapsuleTest.randomBytes(16));
    ByteString address = ByteString.copyFrom(AccountCapsuleTest.randomBytes(32));
    AccountType accountType = AccountType.forNumber(1);
    accountCapsuleTest = new AccountCapsule(accountName, address, accountType);
    byte[] accountByte = accountCapsuleTest.getData();
    accountCapsule = new AccountCapsule(accountByte);
    accountCapsuleTest.setBalance(1111L);
  }

  @AfterClass
  public static void removeDb() {
    Args.clearParam();
    context.destroy();
    FileUtil.deleteDir(new File(dbPath));
  }

  @Test
  public void getDataTest() {
    //test AccountCapsule onstructed function
    Assert.assertEquals(accountCapsule.getInstance().getAccountName(),
        accountCapsuleTest.getInstance().getAccountName());
    Assert.assertEquals(accountCapsule.getInstance().getType(),
        accountCapsuleTest.getInstance().getType());
    Assert.assertEquals(1111, accountCapsuleTest.getBalance());
  }

  @Test
  public void addVotesTest() {
    //test addVote and getVotesList function
    ByteString voteAddress = ByteString.copyFrom(AccountCapsuleTest.randomBytes(32));
    long voteAdd = 10L;
    accountCapsuleTest.addVotes(voteAddress, voteAdd);
    List<Vote> votesList = accountCapsuleTest.getVotesList();
    for (Vote vote :
        votesList) {
      Assert.assertEquals(voteAddress, vote.getVoteAddress());
      Assert.assertEquals(voteAdd, vote.getVoteCount());
    }
  }


  public static byte[] randomBytes(int length) {
    //generate the random number
    byte[] result = new byte[length];
    new Random().nextBytes(result);
    return result;
  }


  @Test
  public void witnessPermissionTest() {
    AccountCapsule accountCapsule =
        new AccountCapsule(
            ByteString.copyFromUtf8("owner"),
            ByteString.copyFrom(ByteArray.fromHexString(OWNER_ADDRESS)),
            AccountType.Normal,
            10000);

    Assert.assertTrue(
        Arrays.equals(ByteArray.fromHexString(OWNER_ADDRESS),
            accountCapsule.getWitnessPermissionAddress()));

    String witnessPermissionAddress =
        Wallet.getAddressPreFixString() + "cc6a17a49648a8ad32055c06f60fa14ae46df912cc";
    accountCapsule = new AccountCapsule(accountCapsule.getInstance().toBuilder().
        setWitnessPermission(Permission.newBuilder().addKeys(
            Key.newBuilder()
                .setAddress(ByteString.copyFrom(ByteArray.fromHexString(witnessPermissionAddress)))
                .build()).
            build()).build());

    Assert.assertTrue(
        Arrays.equals(ByteArray.fromHexString(witnessPermissionAddress),
            accountCapsule.getWitnessPermissionAddress()));
  }
}