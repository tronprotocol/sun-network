package org.tron.common.logsfilter;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.tron.common.logsfilter.trigger.BalanceTrackerTrigger.AssetStatusPojo;
import org.tron.common.logsfilter.trigger.BalanceTrackerTrigger.ConcernTopics;
import org.tron.common.parameter.CommonParameter;
import org.tron.common.runtime.ProgramResult;
import org.tron.common.runtime.vm.LogInfo;
import org.tron.common.utils.Commons;
import org.tron.common.utils.DBConfig;
import org.tron.common.utils.WalletUtil;
import org.tron.core.actuator.VMActuator;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.db.TransactionContext;
import org.tron.core.db.TransactionTrace;
import org.tron.core.store.StoreFactory;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;
import org.tron.protos.contract.SmartContractOuterClass.TriggerSmartContract;

@Slf4j
public class TRC20Utils {

  static VMActuator vmActuator = new VMActuator(true);
  static final String WTRXAddress = "TNUC9Qb1rRpS5CbWLmNMxXBjyFoydXjWFR";

  public static BigInteger getTRC20Decimal(String contractAddress, BlockCapsule baseBlockCap) {
    byte[] data = Hex.decode("313ce567");
    ProgramResult result = triggerFromVM(contractAddress, data, baseBlockCap);
    if (Objects.isNull(result.getException()) && !result.isRevert() && StringUtils
        .isEmpty(result.getRuntimeError())
        && result.getHReturn() != null) {
      try {
        BigInteger ret = toBigInteger(result.getHReturn());
        return ret;
      } catch (Exception e) {
      }
    }

    logger.error(" >>>>> getTRC20Decimal get error, {}", contractAddress);
    return null;

  }


  public static BigInteger hexStrToBigInteger(String hexStr) {
    if (org.apache.commons.lang3.StringUtils.isNotBlank(hexStr)) {
      try {
        return new BigInteger(hexStr, 16);
      } catch (Exception e) {
      }
    }
    return null;
  }

  public static BigInteger toBigInteger(byte[] input) {
    if (input != null && input.length > 0) {
      try {
        String hex = Hex.toHexString(input);
        return hexStrToBigInteger(hex);
      } catch (Exception e) {
      }
    }
    return null;
  }

  public static BigInteger getTRC20Balance(String ownerAddress, String contractAddress,
      BlockCapsule baseBlockCap) {
    byte[] data = Bytes.concat(Hex.decode("70a082310000000000000000000000"),
        Commons.decodeFromBase58Check(ownerAddress));
    ProgramResult result = triggerFromVM(contractAddress, data, baseBlockCap);
    if (Objects.isNull(result.getException()) &&
        !result.isRevert() && StringUtils.isEmpty(result.getRuntimeError())
        && result.getHReturn() != null) {
      try {
        BigInteger ret = toBigInteger(result.getHReturn());
        return ret;
      } catch (Exception e) {
      }
    }

    logger.error(" >>>>> getTRC20Balance get error, {}, ownerAddress:{}", contractAddress, ownerAddress);
    return null;

  }

  public static List<AssetStatusPojo> parseTrc20AssetStatusPojo(BlockCapsule block,
      List<LogInfo> logInfos) {
    List<AssetStatusPojo> ret = new ArrayList<>();

    Set<String> tokenSet = new HashSet<>();

    Map<String, BigInteger> incrementMap = new LinkedHashMap<>();
    Map<String, BigInteger> balanceMap = new LinkedHashMap<>();
    Map<String, BigInteger> decimalMap = new LinkedHashMap<>();
    for (LogInfo logInfo : logInfos) {
      List<String> topics = logInfo.getHexTopics();
      if (CollectionUtils.isEmpty(topics)) {
        continue;
      }

      BigInteger increment = hexStrToBigInteger(logInfo.getHexData());
      if (increment == null) {
        continue;
      }
      String tokenAddress = WalletUtil
          .encode58Check(TransactionTrace.convertToTronAddress(logInfo.getAddress()));
      switch (ConcernTopics.getBySH(topics.get(0))) {
        case TRANSFER:
          if (topics.size() < 3) {
            continue;
          }
          //TransferCase : decrease sender, increase receiver
          String senderAddr = WalletUtil
              .encode58Check(
                  TransactionTrace.convertToTronAddress(logInfo.getTopics().get(1).getLast20Bytes()));
          adjustIncrement(incrementMap, senderAddr, tokenAddress, increment.negate());
          String recAddr = WalletUtil
              .encode58Check(
                  TransactionTrace.convertToTronAddress(logInfo.getTopics().get(2).getLast20Bytes()));
          adjustIncrement(incrementMap, recAddr, tokenAddress, increment);
          tokenSet.add(tokenAddress);
          break;
        case Deposit:
          if (!tokenAddress.equals(WTRXAddress) || topics.size() < 2) {
            continue;
          }
          //DepositCase : increase receiver
          recAddr = WalletUtil
              .encode58Check(
                  TransactionTrace.convertToTronAddress(logInfo.getTopics().get(1).getLast20Bytes()));
          adjustIncrement(incrementMap, recAddr, tokenAddress, increment);
          tokenSet.add(tokenAddress);
          break;
        case Withdrawal:
          if (!tokenAddress.equals(WTRXAddress) || topics.size() < 2) {
            continue;
          }
          //WithdrawalCase : decrease sender
          senderAddr = WalletUtil
              .encode58Check(
                  TransactionTrace.convertToTronAddress(logInfo.getTopics().get(1).getLast20Bytes()));
          adjustIncrement(incrementMap, senderAddr, tokenAddress, increment.negate());
          tokenSet.add(tokenAddress);
          break;
        default:
          continue;
      }

    }


    DBConfig.setDebug(true);
    for (
        String keys : incrementMap.keySet()) {
      // foreach address try to get it's balance.
      String[] key = keys.split(",");
      BigInteger balance = TRC20Utils.getTRC20Balance(key[0], key[1], block);
      if (balance != null) {
        balanceMap.put(keys, balance);
      }
    }
    for (
        String token : tokenSet) {
      BigInteger decimals = TRC20Utils.getTRC20Decimal(token, block);
      if (decimals != null) {
        decimalMap.put(token, decimals);
      }
    }

    DBConfig.setDebug(false);
    logger.debug("incrementMap: {}", incrementMap);
    logger.debug("balanceMap: {}", balanceMap);
    logger.debug("decimalsMap: {}", decimalMap);

    //
    for (
        String keys : incrementMap.keySet()) {
      String[] key = keys.split(",");
      AssetStatusPojo assetStatusPojo = new AssetStatusPojo();
      assetStatusPojo.setAccountAddress(key[0]);
      assetStatusPojo.setTokenAddress(key[1]);
      assetStatusPojo.setIncrementBalance(bigIntegertoString(incrementMap.get(keys)));
      assetStatusPojo.setBalance(bigIntegertoString(balanceMap.get(keys)));
      assetStatusPojo.setDecimals(bigIntegertoString(decimalMap.get(key[1])));
      ret.add(assetStatusPojo);
    }

    return ret;
  }


  private static String bigIntegertoString(BigInteger bigInteger) {
    if (bigInteger != null) {
      return bigInteger.toString();
    }
    return null;
  }

  private static void adjustIncrement(Map<String, BigInteger> incrementMap, String address,
      String token,
      BigInteger wad) {
    BigInteger previous = incrementMap.get(address + "," + token);
    if (previous == null) {
      previous = new BigInteger("0");
    }
    previous = previous.add(wad);
    incrementMap.put(address + "," + token, previous);
  }


  private static ProgramResult triggerFromVM(String contractAddress, byte[] data,
      BlockCapsule baseBlockCap) {
    TriggerSmartContract.Builder build = TriggerSmartContract.newBuilder();
    build.setData(
        ByteString.copyFrom(data));
    build.setOwnerAddress(ByteString.EMPTY);
    build.setCallValue(0);
    build.setCallTokenValue(0);
    build.setTokenId(0);
    build.setContractAddress(ByteString.copyFrom(Commons.decodeFromBase58Check(contractAddress)));
    TransactionCapsule trx = new TransactionCapsule(build.build(),
        ContractType.TriggerSmartContract);
    Transaction.Builder txBuilder = trx.getInstance().toBuilder();
    Transaction.raw.Builder rawBuilder = trx.getInstance().getRawData().toBuilder();
    rawBuilder.setFeeLimit(1000000000L);
    txBuilder.setRawData(rawBuilder);

    TransactionContext context = new TransactionContext(baseBlockCap,
        new TransactionCapsule(txBuilder.build()),
        StoreFactory.getInstance(), true,
        false);

    try {
      vmActuator.validate(context);
      vmActuator.execute(context);
    } catch (Exception e) {
      logger.warn("{} trigger failed!", contractAddress);
    }

    ProgramResult result = context.getProgramResult();
    return result;
  }
}
