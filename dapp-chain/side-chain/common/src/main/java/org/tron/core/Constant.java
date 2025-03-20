package org.tron.core;

import org.tron.common.utils.ByteArray;

public class Constant {

  // whole
  public static final byte[] LAST_HASH = ByteArray.fromString("lastHash");
  public static final String DIFFICULTY = "2001";

  // DB
  public static final String BLOCK_DB_NAME = "block_data";
  public static final String TRANSACTION_DB_NAME = "transaction_data";

  //config for testnet, mainnet, beta
  public static final String TESTNET_CONF = "config.conf";

  //config for junit test
  public static final String TEST_CONF = "config-test.conf";

  public static final String DATABASE_DIR = "storage.directory";

  public static final byte ADD_PRE_FIX_BYTE_MAINNET = (byte) 0x41;   //41 + address
  public static final String ADD_PRE_FIX_STRING_MAINNET = "41";
  public static final byte ADD_PRE_FIX_BYTE_TESTNET = (byte) 0xa0;   //a0 + address
  public static final String ADD_PRE_FIX_STRING_TESTNET = "a0";

  // config for transaction
  public static final long TRANSACTION_MAX_BYTE_SIZE = 500 * 1_024L;
  public static final long MAXIMUM_TIME_UNTIL_EXPIRATION = 24 * 60 * 60 * 1_000L; //one day
  public static final long TRANSACTION_DEFAULT_EXPIRATION_TIME = 60 * 1_000L; //60 seconds
  // config for smart contract
  public static final long SUN_PER_ENERGY = 100; // 1 us = 100 DROP = 100 * 10^-6 TRX
  public static final long ENERGY_LIMIT_IN_CONSTANT_TX = 3_000_000L; // ref: 1 us = 1 energy
  public static final long MAX_RESULT_SIZE_IN_TX = 64; // max 8 * 8 items in result
  public static final long PB_DEFAULT_ENERGY_LIMIT = 0L;
  public static final long CREATOR_DEFAULT_ENERGY_LIMIT = 1000 * 10_000L;
  public static final long ENERGY_LIMIT_IN_CHARGING_OFF = 10_000_000L;
  public static final long ENERGY_LIMIT_ALLOW_DAPP_152 = 100 * 1_000_000L;



  // Numbers
  public static final int ONE_HUNDRED = 100;
  public static final int ONE_THOUSAND = 1000;

  public static final byte[] ZTRON_EXPANDSEED_PERSONALIZATION = {'Z', 't', 'r', 'o', 'n', '_', 'E',
          'x',
          'p', 'a', 'n', 'd', 'S', 'e', 'e', 'd'};
  public static final int ZC_DIVERSIFIER_SIZE = 11;
  public static final int ZC_OUTPUT_DESC_MAX_SIZE = 10;


  /**
   * normal transaction is 0 representing normal transaction unexecuted deferred transaction is 1
   * representing unexecuted deferred transaction executing deferred transaction is 2 representing
   * executing deferred transaction
   */
  public static final int NORMALTRANSACTION = 0;
  public static final int UNEXECUTEDDEFERREDTRANSACTION = 1;
  public static final int EXECUTINGDEFERREDTRANSACTION = 2;

  // Configuration items
  public static final String NET_TYPE = "net.type";
  public static final String TESTNET = "testnet";
  public static final String LOCAL_WITENSS = "localwitness";
  public static final String LOCAL_WITNESS_ACCOUNT_ADDRESS = "localWitnessAccountAddress";
  public static final String LOCAL_WITNESS_KEYSTORE = "localwitnesskeystore";
  public static final String VM_SUPPORT_CONSTANT = "vm.supportConstant";
  public static final String VM_UPDATEGATEWAY_V1_0_2 = "vm.updateGateway_v1_0_2";
  public static final String VM_MIN_TIME_RATIO = "vm.minTimeRatio";
  public static final String VM_MAX_TIME_RATIO = "vm.maxTimeRatio";
  public static final String VM_LONG_RUNNING_TIME = "vm.longRunningTime";

  public static final String ROCKSDB = "ROCKSDB";

  public static final String GENESIS_BLOCK = "genesis.block";
  public static final String GENESIS_BLOCK_TIMESTAMP = "genesis.block.timestamp";
  public static final String GENESIS_BLOCK_PARENTHASH = "genesis.block.parentHash";
  public static final String GENESIS_BLOCK_ASSETS = "genesis.block.assets";
  public static final String GENESIS_BLOCK_WITNESSES = "genesis.block.witnesses";

  public static final String BLOCK_NEED_SYNC_CHECK = "block.needSyncCheck";
  public static final String NODE_DISCOVERY_ENABLE = "node.discovery.enable";
  public static final String NODE_DISCOVERY_PERSIST = "node.discovery.persist";
  public static final String NODE_CONNECTION_TIMEOUT = "node.connection.timeout";
  public static final String NODE_CHANNEL_READ_TIMEOUT = "node.channel.read.timeout";
  public static final String NODE_MAX_ACTIVE_NODES = "node.maxActiveNodes";
  public static final String NODE_MAX_ACTIVE_NODES_WITH_SAMEIP = "node.maxActiveNodesWithSameIp";
  public static final String NODE_MIN_PARTICIPATION_RATE = "node.minParticipationRate";
  public static final String NODE_LISTEN_PORT = "node.listen.port";
  public static final String NODE_DISCOVERY_PUBLIC_HOME_NODE = "node.discovery.public.home.node";

  public static final String NODE_P2P_PING_INTERVAL = "node.p2p.pingInterval";
  public static final String NODE_P2P_VERSION = "node.p2p.version";
  public static final String NODE_RPC_PORT = "node.rpc.port";
  public static final String NODE_RPC_SOLIDITY_PORT = "node.rpc.solidityPort";
  public static final String NODE_HTTP_FULLNODE_PORT = "node.http.fullNodePort";
  public static final String NODE_HTTP_SOLIDITY_PORT = "node.http.solidityPort";
  public static final String NODE_HTTP_FULLNODE_ENABLE = "node.http.fullNodeEnable";
  public static final String NODE_HTTP_SOLIDITY_ENABLE = "node.http.solidityEnable";

  public static final String NODE_RPC_THREAD = "node.rpc.thread";
  public static final String NODE_SOLIDITY_THREADS = "node.solidity.threads";

  public static final String NODE_RPC_MAX_CONCURRENT_CALLS_PER_CONNECTION = "node.rpc.maxConcurrentCallsPerConnection";
  public static final String NODE_RPC_FLOW_CONTROL_WINDOW = "node.rpc.flowControlWindow";
  public static final String NODE_RPC_MAX_CONNECTION_IDLE_IN_MILLIS = "node.rpc.maxConnectionIdleInMillis";
  public static final String NODE_PRODUCED_TIMEOUT = "node.blockProducedTimeOut";
  public static final String NODE_MAX_HTTP_CONNECT_NUMBER = "node.maxHttpConnectNumber";

  public static final String NODE_NET_MAX_TRX_PER_SECOND = "node.netMaxTrxPerSecond";
  public static final String NODE_RPC_MAX_CONNECTION_AGE_IN_MILLIS = "node.rpc.maxConnectionAgeInMillis";
  public static final String NODE_RPC_MAX_MESSAGE_SIZE = "node.rpc.maxMessageSize";

  public static final String NODE_RPC_MAX_HEADER_LIST_ISZE = "node.rpc.maxHeaderListSize";

  public static final String BLOCK_MAINTENANCE_TIME_INTERVAL = "block.maintenanceTimeInterval";
  public static final String BLOCK_PROPOSAL_EXPIRE_TIME = "block.proposalExpireTime";

  public static final String BLOCK_CHECK_FROZEN_TIME = "block.checkFrozenTime";

  public static final String COMMITTEE_ALLOW_CREATION_OF_CONTRACTS = "committee.allowCreationOfContracts";

  public static final String COMMITTEE_ALLOW_MULTI_SIGN = "committee.allowMultiSign";

  public static final String COMMITTEE_ALLOW_ADAPTIVE_ENERGY = "committee.allowAdaptiveEnergy";

  public static final String COMMITTEE_ALLOW_DELEGATE_RESOURCE = "committee.allowDelegateResource";

  public static final String COMMITTEE_ALLOW_SAME_TOKEN_NAME = "committee.allowSameTokenName";

  public static final String COMMITTEE_ALLOW_TVM_TRANSFER_TRC10 = "committee.allowTvmTransferTrc10";

  public static final String COMMITTEE_ALLOW_TVM_CONSTANTINOPLE = "committee.allowTvmConstantinople";

  public static final String COMMITTEE_ALLOW_TVM_SOLIDITY059 = "committee.allowTvmSolidity059";

  public static final String NODE_TCP_NETTY_WORK_THREAD_NUM = "node.tcpNettyWorkThreadNum";

  public static final String NODE_UDP_NETTY_WORK_THREAD_NUM = "node.udpNettyWorkThreadNum";

  public static final String NODE_TRUST_NODE = "node.trustNode";

  public static final String NODE_VALIDATE_SIGN_THREAD_NUM = "node.validateSignThreadNum";

  public static final String NODE_WALLET_EXTENSION_API = "node.walletExtensionApi";

  public static final String NODE_CONNECT_FACTOR = "node.connectFactor";

  public static final String NODE_ACTIVE_CONNECT_FACTOR = "node.activeConnectFactor";

  public static final String NODE_DISCONNECT_NUMBER_FACTOR = "node.disconnectNumberFactor";

  public static final String NODE_MAX_CONNECT_NUMBER_FACTOR = "node.maxConnectNumberFactor";

  public static final String NODE_RECEIVE_TCP_MIN_DATA_LENGTH = "node.receiveTcpMinDataLength";

  public static final String NODE_IS_OPEN_FULL_TCP_DISCONNECT = "node.isOpenFullTcpDisconnect";

  public static final String STORAGE_NEEDTO_UPDATE_ASSET = "storage.needToUpdateAsset";

  public static final String  TRX_REFERENCE_BLOCK = "trx.reference.block";

  public static final String TRX_EXPIRATION_TIME_IN_MILLIS_SECONDS = "trx.expiration.timeInMilliseconds";

  public static final String NODE_RPC_MIN_EFFECTIVE_CONNECTION = "node.rpc.minEffectiveConnection";

  public static final String ENERGY_LIMIT_BLOCK_NUM = "enery.limit.block.num";

  public static final String VM_TRACE = "vm.vmTrace";

  public static final String VM_SAVE_INTERNAL_TX = "vm.saveInternalTx";

//  public static final String COMMITTEE_ALLOW_SHIELDED_TRANSACTION = "committee.allowShieldedTransaction";

  public static final String EVENT_SUBSCRIBE = "event.subscribe";

  public static final String EVENT_SUBSCRIBE_FILTER = "event.subscribe.filter";

  public static final String NODE_FULLNODE_ALLOW_SHIELDED_TRANSACTION = "node.fullNodeAllowShieldedTransaction";

  public static final String NODE_ZEN_TOKENID = "node.zenTokenId";

  public static final String COMMITTEE_ALLOW_PROTO_FILTER_NUM = "committee.allowProtoFilterNum";

  public static final String COMMITTEE_ALLOW_ACCOUNT_STATE_ROOT = "committee.allowAccountStateRoot";

  public static final String NODE_VALID_CONTRACT_PROTO_THREADS = "node.validContractProto.threads";

  public static final String NODE_ACTIVE = "node.active";

  public static final String NODE_PASSIVE = "node.passive";

  public static final String NODE_FAST_FORWARD = "node.fastForward";

  public static final String NODE_SHIELDED_TRANS_IN_PENDING_MAX_COUNTS = "node.shieldedTransInPendingMaxCounts";

  public static final String RATE_LIMITER = "rate.limiter";

  public static final String COMMITTEE_CHANGED_DELEGATION = "committee.changedDelegation";

  public static final String CRYPTO_ENGINE = "crypto.engine";

  public static final String ECKey_ENGINE = "ECKey";

  public static final String USE_NATIVE_QUEUE = "event.subscribe.native.useNativeQueue";

  public static final String NATIVE_QUEUE_BIND_PORT = "event.subscribe.native.bindport";

  public static final String NATIVE_QUEUE_SEND_LENGTH = "event.subscribe.native.sendqueuelength";

  public static final String EVENT_SUBSCRIBE_PATH = "event.subscribe.path";
  public static final String EVENT_SUBSCRIBE_SERVER = "event.subscribe.server";
  public static final String EVENT_SUBSCIBE_DB_CONFIG = "event.subscribe.dbconfig";
  public static final String EVENT_SUBSCRIBE_TOPICS = "event.subscribe.topics";
  public static final String EVENT_SUBSCRIBE_FROM_BLOCK = "event.subscribe.filter.fromblock";
  public static final String EVENT_SUBSCRIBE_TO_BLOCK = "event.subscribe.filter.toblock";
  public static final String EVENT_SUBSCRIBE_CONTRACT_ADDRESS = "event.subscribe.filter.contractAddress";
  public static final String EVENT_SUBSCRIBE_CONTRACT_TOPIC = "event.subscribe.filter.contractTopic";

  public static final String NODE_DISCOVERY_BIND_IP = "node.discovery.bind.ip";

  public static final String NODE_DISCOVERY_EXTENNAL_IP = "node.discovery.external.ip";
  public static final String AMAZONAWS_URL = "http://checkip.amazonaws.com";

  public static final String NODE_BACKUP_PRIORITY = "node.backup.priority";
  public static final String NODE_BACKUP_PORT = "node.backup.port";
  public static final String NODE_BACKUP_MEMBERS = "node.backup.members";

  public static final String STORAGE_BACKUP_ENABLE = "storage.backup.enable";
  public static final String STORAGE_BACKUP_PROP_PATH = "storage.backup.propPath";

  public static final String ACTUATOR_WHITELIST = "actuator.whitelist";


  // SideChain
  public static final String SUN_TOKEN_ID = "2000000";
  public static final long MICRO_SUN_TOKEN_PER_ENERGY = 100;
  public static final String TRON_ZERO_ADDRESS_HEX = "410000000000000000000000000000000000000000";
  public static final String GATEWAY_CODE_V_1_0_2_HASH = "ddc918e325efc982957c0e7290a3c916854743572719d7fcad751369c1e90ee8";
}
