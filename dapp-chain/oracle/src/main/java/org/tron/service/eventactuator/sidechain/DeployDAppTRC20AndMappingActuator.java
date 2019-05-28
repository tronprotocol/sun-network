package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.DeployDAppTRC20AndMappingEvent;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class DeployDAppTRC20AndMappingActuator extends Actuator {

  // "event DeployDAppTRC20AndMapping(address developer, address mainChainAddress, address sideChainAddress);"


  DeployDAppTRC20AndMappingEvent event;

  // NOTE: event: PB(data0, data1, data2..., txId, will_task_enum) 其中这里面的txId是发出这个event的交易的txId
  // NOTE: transaction: PB(tx, txId, task_enum) 其中这里面的txId是处理这个event所构造的新交易的txId
  // NOTE: event库和transaction库需要使用同一个key！！使用event所在交易的txId即可！！

  // NOTE: 在主线程里面，1. 收到event之后，仅仅是生成event库，不会失败和卡住，就是存储！然后commit
  // NOTE: 在子线程里面，2. 将event传参，先构造交易，存库，sleep，然后发送，然后提交check

  // NOTE: 2中 构造失败或者存库失败了：下次从event库中读取重新构造！从event中需要知道tx库有没有对应txId，event有，但是tx中没有的，需要重新从event来一遍
  // NOTE: 2中 存库后面，check失败了：从sleep开始往后走到提交check
  // NOTE: 2中 存库后面，check成功了：先删除event(若有)，接着删除tx(若有)(如果先删除tx，接着删除event，两个删除中间有问题了，那么会重复根据event构造交易！！不能出现)，最好是一个session
  // NOTE: 或者置 标记位

  // NOTE: 下次启动的时候：
  // NOTE: 先查event有，但是tx没有的，重新把 2 走一遍
  // NOTE: 接着查tx，有，重新走一遍sleep之后的流程
  // NOTE: 需要查tx有的，但是event中没有的吗？不需要！


  public DeployDAppTRC20AndMappingActuator(String developer, String mainChainAddress,
      String sideChainAddress, String transactionId) {
    ByteString developerBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(developer));
    ByteString mainChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(mainChainAddress));
    ByteString sideChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(sideChainAddress));
    ByteString transactionIdBS = ByteString.copyFrom(ByteArray.fromHexString(transactionId));
    this.type = EventType.DEPOSIT_TRC10_EVENT;
    this.event = DeployDAppTRC20AndMappingEvent.newBuilder().setDeveloper(developerBS)
        .setMainchainAddress(mainChainAddressBS)
        .setSidechainAddress(sideChainAddressBS)
        .setTransactionId(transactionIdBS).setWillTaskEnum(TaskEnum.MAIN_CHAIN).build();
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return transactionExtensionCapsule;
    }

    String developerStr = WalletUtil.encode58Check(event.getDeveloper().toByteArray());
    String mainChainAddressStr = WalletUtil
        .encode58Check(event.getMainchainAddress().toByteArray());
    String sideChainAddressStr = WalletUtil
        .encode58Check(event.getSidechainAddress().toByteArray());
    String transactionIdStr = ByteArray.toHexString(event.getTransactionId().toByteArray());

    logger.info("DeployDAppTRC20AndMappingActuator, developer: {}, mainChainAddress: {},"
            + " sideChainAddress: {}, transactionId: {}", developerStr, mainChainAddressStr,
        sideChainAddressStr, transactionIdStr);

    Transaction tx = MainChainGatewayApi
        .addTokenMappingTransaction(mainChainAddressStr, sideChainAddressStr);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);
    return this.transactionExtensionCapsule;
  }

  @Override
  public EventMsg getMessage() {
    return EventMsg.newBuilder().setParameter(Any.pack(this.event)).setType(this.type).build();
  }

  @Override
  public byte[] getKey() {
    return event.getTransactionId().toByteArray();
  }
}