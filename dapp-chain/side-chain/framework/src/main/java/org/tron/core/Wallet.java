/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */

package org.tron.core;

import static org.tron.common.utils.Commons.ADDRESS_SIZE;
import static org.tron.common.utils.Commons.decodeFromBase58Check;
import static org.tron.common.utils.DecodeUtil.addressPreFixByte;
import static org.tron.common.utils.WalletUtil.checkPermissionOprations;
import static org.tron.core.config.Parameter.DatabaseConstants.EXCHANGE_COUNT_LIMIT_MAX;
import static org.tron.core.config.Parameter.DatabaseConstants.PROPOSAL_COUNT_LIMIT_MAX;
import static org.tron.core.config.args.Parameter.ChainConstant.BLOCK_PRODUCED_INTERVAL;


import com.google.common.base.CaseFormat;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.AccountNetMessage;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.GrpcAPI.Address;
import org.tron.api.GrpcAPI.AssetIssueList;
import org.tron.api.GrpcAPI.BlockList;
import org.tron.api.GrpcAPI.BytesMessage;
import org.tron.api.GrpcAPI.DecryptNotes;
import org.tron.api.GrpcAPI.DecryptNotes.NoteTx;
import org.tron.api.GrpcAPI.DelegatedResourceList;
import org.tron.api.GrpcAPI.DiversifierMessage;
import org.tron.api.GrpcAPI.ExchangeList;
import org.tron.api.GrpcAPI.ExpandedSpendingKeyMessage;
import org.tron.api.GrpcAPI.IncomingViewingKeyMessage;
import org.tron.api.GrpcAPI.NfParameters;
import org.tron.api.GrpcAPI.Node;
import org.tron.api.GrpcAPI.NodeList;
import org.tron.api.GrpcAPI.NoteParameters;
import org.tron.api.GrpcAPI.NumberMessage;
import org.tron.api.GrpcAPI.PaymentAddressMessage;
import org.tron.api.GrpcAPI.PrivateParameters;
import org.tron.api.GrpcAPI.PrivateParametersWithoutAsk;
import org.tron.api.GrpcAPI.ReceiveNote;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.Return.response_code;
import org.tron.api.GrpcAPI.SideChainProposalList;
import org.tron.api.GrpcAPI.ShieldedAddressInfo;
import org.tron.api.GrpcAPI.SpendAuthSigParameters;
import org.tron.api.GrpcAPI.SpendNote;
import org.tron.api.GrpcAPI.SpendResult;
import org.tron.api.GrpcAPI.TransactionApprovedList;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.GrpcAPI.TransactionExtention.Builder;
import org.tron.api.GrpcAPI.TransactionInfoList;
import org.tron.api.GrpcAPI.TransactionSignWeight;
import org.tron.api.GrpcAPI.TransactionSignWeight.Result;
import org.tron.api.GrpcAPI.WitnessList;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.SignInterface;
import org.tron.common.crypto.SignUtils;
import org.tron.common.overlay.discover.node.NodeHandler;
import org.tron.common.overlay.discover.node.NodeManager;
import org.tron.common.overlay.message.Message;
import org.tron.common.runtime.ProgramResult;
import org.tron.common.storage.DepositImpl;
import org.tron.common.utils.*;
import org.tron.common.zksnark.IncrementalMerkleTreeContainer;
import org.tron.common.zksnark.IncrementalMerkleVoucherContainer;
import org.tron.common.zksnark.JLibrustzcash;
import org.tron.common.zksnark.LibrustzcashParam.ComputeNfParams;
import org.tron.common.zksnark.LibrustzcashParam.CrhIvkParams;
import org.tron.common.zksnark.LibrustzcashParam.IvkToPkdParams;
import org.tron.common.zksnark.LibrustzcashParam.SpendSigParams;
import org.tron.core.actuator.Actuator;
import org.tron.core.actuator.ActuatorFactory;
import org.tron.core.actuator.VMActuator;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.AssetIssueCapsule;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.BlockCapsule.BlockId;
import org.tron.core.capsule.BytesCapsule;
import org.tron.core.capsule.ContractCapsule;
import org.tron.core.capsule.DelegatedResourceAccountIndexCapsule;
import org.tron.core.capsule.DelegatedResourceCapsule;
import org.tron.core.capsule.ExchangeCapsule;
import org.tron.core.capsule.IncrementalMerkleTreeCapsule;
import org.tron.core.capsule.IncrementalMerkleVoucherCapsule;
import org.tron.core.capsule.PedersenHashCapsule;
import org.tron.core.capsule.ProposalCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.capsule.TransactionInfoCapsule;
import org.tron.core.capsule.TransactionResultCapsule;
import org.tron.core.capsule.TransactionRetCapsule;
import org.tron.core.capsule.WitnessCapsule;
import org.tron.core.config.args.Args;
import org.tron.core.db.BandwidthProcessor;
import org.tron.core.db.EnergyProcessor;
import org.tron.core.db.Manager;
import org.tron.core.db.TransactionContext;
import org.tron.core.exception.AccountResourceInsufficientException;
import org.tron.core.exception.BadItemException;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.exception.DupTransactionException;
import org.tron.core.exception.HeaderNotFound;
import org.tron.core.exception.ItemNotFoundException;
import org.tron.core.exception.NonUniqueObjectException;
import org.tron.core.exception.PermissionException;
import org.tron.core.exception.SignatureFormatException;
import org.tron.core.exception.StoreException;
import org.tron.core.exception.TaposException;
import org.tron.core.exception.TooBigTransactionException;
import org.tron.core.exception.TransactionExpirationException;
import org.tron.core.exception.VMIllegalException;
import org.tron.core.exception.ValidateSignatureException;
import org.tron.core.exception.ZksnarkException;
import org.tron.core.net.TronNetDelegate;
import org.tron.core.net.TronNetService;
import org.tron.core.net.message.TransactionMessage;
import org.tron.core.store.AccountIdIndexStore;
import org.tron.core.store.AccountStore;
import org.tron.core.store.ContractStore;
import org.tron.core.store.StoreFactory;
import org.tron.core.utils.TransactionUtil;
import org.tron.core.vm.config.VMConfig;
import org.tron.core.zen.ZenTransactionBuilder;
import org.tron.core.zen.address.DiversifierT;
import org.tron.core.zen.address.ExpandedSpendingKey;
import org.tron.core.zen.address.IncomingViewingKey;
import org.tron.core.zen.address.KeyIo;
import org.tron.core.zen.address.PaymentAddress;
import org.tron.core.zen.address.SpendingKey;
import org.tron.core.zen.note.Note;
import org.tron.core.zen.note.NoteEncryption.Encryption;
import org.tron.core.zen.note.OutgoingPlaintext;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.DelegatedResourceAccountIndex;
import org.tron.protos.Protocol.Exchange;
import org.tron.protos.Protocol.Permission;
import org.tron.protos.Protocol.Permission.PermissionType;
import org.tron.protos.Protocol.SideChainProposal;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;
import org.tron.protos.Protocol.Transaction.Result.code;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.Protocol.TransactionSign;
import org.tron.protos.contract.AssetIssueContractOuterClass.AssetIssueContract;
import org.tron.protos.contract.BalanceContract.TransferContract;
import org.tron.protos.contract.ShieldContract.IncrementalMerkleTree;
import org.tron.protos.contract.ShieldContract.IncrementalMerkleVoucherInfo;
import org.tron.protos.contract.ShieldContract.OutputPoint;
import org.tron.protos.contract.ShieldContract.OutputPointInfo;
import org.tron.protos.contract.ShieldContract.PedersenHash;
import org.tron.protos.contract.ShieldContract.ReceiveDescription;
import org.tron.protos.contract.ShieldContract.ShieldedTransferContract;
import org.tron.protos.contract.SmartContractOuterClass.CreateSmartContract;
import org.tron.protos.contract.SmartContractOuterClass.SmartContract;
import org.tron.protos.contract.SmartContractOuterClass.TriggerSmartContract;
import org.tron.common.utils.Base58;

@Slf4j
@Component
public class Wallet {

  private static final String SHIELDED_ID_NOT_ALLOWED = "ShieldedTransactionApi is not allowed";
  private static final String PAYMENT_ADDRESS_FORMAT_WRONG = "paymentAddress format is wrong";
  private static String addressPreFixString = Constant.ADD_PRE_FIX_STRING_MAINNET;//default testnet
  @Getter
  private final SignInterface cryptoEngine;
  @Autowired
  private TronNetService tronNetService;
  @Autowired
  private TronNetDelegate tronNetDelegate;
  @Autowired
  private Manager dbManager;
  @Autowired
  private NodeManager nodeManager;
  private int minEffectiveConnection = Args.getInstance().getMinEffectiveConnection();

  /**
   * Creates a new Wallet with a random ECKey.
   */
  public Wallet() {
    this.cryptoEngine = SignUtils.getGeneratedRandomSign(Utils.getRandom(),
        Args.getInstance().isECKeyCryptoEngine());
  }

  /**
   * Creates a Wallet with an existing ECKey.
   */
  public Wallet(final SignInterface cryptoEngine) {
    this.cryptoEngine = cryptoEngine;
    logger.info("wallet address: {}", ByteArray.toHexString(this.cryptoEngine.getAddress()));
  }


  public static String getAddressPreFixString() {
    return addressPreFixString;
  }

  public static void setAddressPreFixString(String addressPreFixString) {
    Wallet.addressPreFixString = addressPreFixString;
  }

  public static byte getAddressPreFixByte() {
    return addressPreFixByte;
  }

  public static void setAddressPreFixByte(byte addressPreFixByte) {
    DecodeUtil.addressPreFixByte = addressPreFixByte;
  }

  public static boolean addressValid(byte[] address) {
    if (ArrayUtils.isEmpty(address)) {
      logger.warn("Warning: Address is empty !!");
      return false;
    }
    if (address.length != ADDRESS_SIZE / 2) {
      logger.warn(
          "Warning: Address length requires " + ADDRESS_SIZE + " but " + address.length
              + " !!");
      return false;
    }
    if (address[0] != addressPreFixByte) {
      logger.warn("Warning: Address requires a prefix with " + addressPreFixByte + " but "
          + address[0] + " !!");
      return false;
    }
    //Other rule;
    return true;
  }

  private static byte[] decode58Check(String input) {
    byte[] decodeCheck = Base58.decode(input);
    if (decodeCheck.length <= 4) {
      return null;
    }
    byte[] decodeData = new byte[decodeCheck.length - 4];
    System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
    byte[] hash0 = Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), decodeData);
    byte[] hash1 = Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), hash0);
    if (hash1[0] == decodeCheck[decodeData.length] &&
        hash1[1] == decodeCheck[decodeData.length + 1] &&
        hash1[2] == decodeCheck[decodeData.length + 2] &&
        hash1[3] == decodeCheck[decodeData.length + 3]) {
      return decodeData;
    }
    return null;
  }


  public static byte[] decodeFromBase58Check(String addressBase58) {
    if (StringUtils.isEmpty(addressBase58)) {
      logger.warn("Warning: Address is empty !!");
      return null;
    }
    byte[] address = decode58Check(addressBase58);
    if (address == null) {
      return null;
    }

    if (!addressValid(address)) {
      return null;
    }

    return address;
  }

//  public ShieldAddress generateShieldAddress() {
//    ShieldAddress.Builder builder = ShieldAddress.newBuilder();
//    ShieldAddressGenerator shieldAddressGenerator = new ShieldAddressGenerator();
//
//    byte[] privateKey = shieldAddressGenerator.generatePrivateKey();
//    byte[] publicKey = shieldAddressGenerator.generatePublicKey(privateKey);
//
//    byte[] privateKeyEnc = shieldAddressGenerator.generatePrivateKeyEnc(privateKey);
//    byte[] publicKeyEnc = shieldAddressGenerator.generatePublicKeyEnc(privateKeyEnc);
//
//    byte[] addPrivate = ByteUtil.merge(privateKey, privateKeyEnc);
//    byte[] addPublic = ByteUtil.merge(publicKey, publicKeyEnc);
//
//    builder.setPrivateAddress(ByteString.copyFrom(addPrivate));
//    builder.setPublicAddress(ByteString.copyFrom(addPublic));
//    return builder.build();
//  }

  public static String makeUpperCamelMethod(String originName) {
    return "get" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, originName)
        .replace("_", "");
  }

  private static byte[] getSelector(byte[] data) {
    if (data == null ||
        data.length < 4) {
      return null;
    }

    byte[] ret = new byte[4];
    System.arraycopy(data, 0, ret, 0, 4);
    return ret;
  }

  /**
   * Create a transaction.
   */
  /*public Transaction createTransaction(byte[] address, String to, long amount) {
    long balance = getBalance(address);
    return new TransactionCapsule(address, to, amount, balance, utxoStore).getInstance();
  } */

  public byte[] getAddress() {
    return cryptoEngine.getAddress();
  }

  public Account getAccount(Account account) {
    AccountStore accountStore = dbManager.getAccountStore();
    AccountCapsule accountCapsule = accountStore.get(account.getAddress().toByteArray());
    if (accountCapsule == null) {
      return null;
    }
    BandwidthProcessor processor = new BandwidthProcessor(dbManager);
    processor.updateUsage(accountCapsule);

    EnergyProcessor energyProcessor = new EnergyProcessor(dbManager.getDynamicPropertiesStore(),
        dbManager.getAccountStore());
    energyProcessor.updateUsage(accountCapsule);

    long genesisTimeStamp = dbManager.getGenesisBlock().getTimeStamp();
    accountCapsule.setLatestConsumeTime(genesisTimeStamp
        + BLOCK_PRODUCED_INTERVAL * accountCapsule.getLatestConsumeTime());
    accountCapsule.setLatestConsumeFreeTime(genesisTimeStamp
        + BLOCK_PRODUCED_INTERVAL * accountCapsule.getLatestConsumeFreeTime());
    accountCapsule.setLatestConsumeTimeForEnergy(genesisTimeStamp
        + BLOCK_PRODUCED_INTERVAL * accountCapsule.getLatestConsumeTimeForEnergy());

    return accountCapsule.getInstance();
  }

  public Account getAccountById(Account account) {
    AccountStore accountStore = dbManager.getAccountStore();
    AccountIdIndexStore accountIdIndexStore = dbManager.getAccountIdIndexStore();
    byte[] address = accountIdIndexStore.get(account.getAccountId());
    if (address == null) {
      return null;
    }
    AccountCapsule accountCapsule = accountStore.get(address);
    if (accountCapsule == null) {
      return null;
    }
    BandwidthProcessor processor = new BandwidthProcessor(dbManager);
    processor.updateUsage(accountCapsule);

    EnergyProcessor energyProcessor = new EnergyProcessor(dbManager.getDynamicPropertiesStore(),
        dbManager.getAccountStore());
    energyProcessor.updateUsage(accountCapsule);

    long genesisTimeStamp = dbManager.getGenesisBlock().getTimeStamp();
    accountCapsule.setLatestConsumeTime(genesisTimeStamp
        + BLOCK_PRODUCED_INTERVAL * accountCapsule.getLatestConsumeTime());
    accountCapsule.setLatestConsumeFreeTime(genesisTimeStamp
        + BLOCK_PRODUCED_INTERVAL * accountCapsule.getLatestConsumeFreeTime());
    accountCapsule.setLatestConsumeTimeForEnergy(genesisTimeStamp
        + BLOCK_PRODUCED_INTERVAL * accountCapsule.getLatestConsumeTimeForEnergy());

    return accountCapsule.getInstance();
  }

  /**
   * Create a transaction by contract.
   */
  @Deprecated
  public Transaction createTransaction(TransferContract contract) {
    AccountStore accountStore = dbManager.getAccountStore();
    return new TransactionCapsule(contract, accountStore).getInstance();
  }

  public TransactionCapsule createTransactionCapsuleWithoutValidate(
      com.google.protobuf.Message message,
      ContractType contractType) {
    TransactionCapsule trx = new TransactionCapsule(message, contractType);
    try {
      BlockId blockId = dbManager.getHeadBlockId();
      if ("solid".equals(Args.getInstance().getTrxReferenceBlock())) {
        blockId = dbManager.getSolidBlockId();
      }
      trx.setReference(blockId.getNum(), blockId.getBytes());
      long expiration =
          dbManager.getHeadBlockTimeStamp() + Args.getInstance()
              .getTrxExpirationTimeInMilliseconds();
      trx.setExpiration(expiration);
      trx.setTimestamp();
    } catch (Exception e) {
      logger.error("Create transaction capsule failed.", e);
    }
    return trx;
  }

  public TransactionCapsule createTransactionCapsule(com.google.protobuf.Message message,
      ContractType contractType) throws ContractValidateException {
    TransactionCapsule trx = new TransactionCapsule(message, contractType);
    if (contractType != ContractType.CreateSmartContract
        && contractType != ContractType.TriggerSmartContract) {
      List<Actuator> actList = ActuatorFactory.createActuator(trx, dbManager);
      for (Actuator act : actList) {
        act.validate();
      }
    }

    if (contractType == ContractType.CreateSmartContract) {

      CreateSmartContract contract = ContractCapsule
          .getSmartContractFromTransaction(trx.getInstance());
      long percent = contract.getNewContract().getConsumeUserResourcePercent();
      if (percent < 0 || percent > 100) {
        throw new ContractValidateException("percent must be >= 0 and <= 100");
      }
    }

    try {
      BlockId blockId = dbManager.getHeadBlockId();
      if ("solid".equals(Args.getInstance().getTrxReferenceBlock())) {
        blockId = dbManager.getSolidBlockId();
      }
      trx.setReference(blockId.getNum(), blockId.getBytes());
      long expiration =
          dbManager.getHeadBlockTimeStamp() + Args.getInstance()
              .getTrxExpirationTimeInMilliseconds();
      trx.setExpiration(expiration);
      trx.setTimestamp();
    } catch (Exception e) {
      logger.error("Create transaction capsule failed.", e);
    }
    return trx;
  }

  /**
   * Broadcast a transaction.
   */
  public GrpcAPI.Return broadcastTransaction(Transaction signaturedTransaction) {
    GrpcAPI.Return.Builder builder = GrpcAPI.Return.newBuilder();
    TransactionCapsule trx = new TransactionCapsule(signaturedTransaction);
    trx.setTime(System.currentTimeMillis());
    try {
      Message message = new TransactionMessage(signaturedTransaction.toByteArray());
      if (minEffectiveConnection != 0) {
        if (tronNetDelegate.getActivePeer().isEmpty()) {
          logger
              .warn("Broadcast transaction {} has failed, no connection.", trx.getTransactionId());
          return builder.setResult(false).setCode(response_code.NO_CONNECTION)
              .setMessage(ByteString.copyFromUtf8("no connection"))
              .build();
        }

        int count = (int) tronNetDelegate.getActivePeer().stream()
            .filter(p -> !p.isNeedSyncFromUs() && !p.isNeedSyncFromPeer())
            .count();

        if (count < minEffectiveConnection) {
          String info = "effective connection:" + count + " lt minEffectiveConnection:"
              + minEffectiveConnection;
          logger.warn("Broadcast transaction {} has failed, {}.", trx.getTransactionId(), info);
          return builder.setResult(false).setCode(response_code.NOT_ENOUGH_EFFECTIVE_CONNECTION)
              .setMessage(ByteString.copyFromUtf8(info))
              .build();
        }
      }

      if (dbManager.isTooManyPending()) {
        logger
            .warn("Broadcast transaction {} has failed, too many pending.", trx.getTransactionId());
        return builder.setResult(false).setCode(response_code.SERVER_BUSY).build();
      }

      if (dbManager.getTransactionIdCache().getIfPresent(trx.getTransactionId()) != null) {
        logger.warn("Broadcast transaction {} has failed, it already exists.",
            trx.getTransactionId());
        return builder.setResult(false).setCode(response_code.DUP_TRANSACTION_ERROR).build();
      } else {
        dbManager.getTransactionIdCache().put(trx.getTransactionId(), true);
      }
      trx.resetResult();
      dbManager.pushTransaction(trx);
      tronNetService.broadcast(message);
      logger.info("Broadcast transaction {} successfully.", trx.getTransactionId());
      return builder.setResult(true).setCode(response_code.SUCCESS).build();
    } catch (ValidateSignatureException e) {
      logger.error("Broadcast transaction {} failed, {}.", trx.getTransactionId(), e.getMessage());
      return builder.setResult(false).setCode(response_code.SIGERROR)
          .setMessage(ByteString.copyFromUtf8("validate signature error " + e.getMessage()))
          .build();
    } catch (ContractValidateException e) {
      logger.error("Broadcast transaction {} failed, {}.", trx.getTransactionId(), e.getMessage());
      return builder.setResult(false).setCode(response_code.CONTRACT_VALIDATE_ERROR)
          .setMessage(ByteString.copyFromUtf8("contract validate error : " + e.getMessage()))
          .build();
    } catch (ContractExeException e) {
      logger.error("Broadcast transaction {} failed, {}.", trx.getTransactionId(), e.getMessage());
      return builder.setResult(false).setCode(response_code.CONTRACT_EXE_ERROR)
          .setMessage(ByteString.copyFromUtf8("contract execute error : " + e.getMessage()))
          .build();
    } catch (AccountResourceInsufficientException e) {
      logger.error("Broadcast transaction {} failed, {}.", trx.getTransactionId(), e.getMessage());
      return builder.setResult(false).setCode(response_code.BANDWITH_ERROR)
          .setMessage(ByteString.copyFromUtf8("AccountResourceInsufficient error"))
          .build();
    } catch (DupTransactionException e) {
      logger.error("Broadcast transaction {} failed, {}.", trx.getTransactionId(), e.getMessage());
      return builder.setResult(false).setCode(response_code.DUP_TRANSACTION_ERROR)
          .setMessage(ByteString.copyFromUtf8("dup transaction"))
          .build();
    } catch (TaposException e) {
      logger.error("Broadcast transaction {} failed, {}.", trx.getTransactionId(), e.getMessage());
      return builder.setResult(false).setCode(response_code.TAPOS_ERROR)
          .setMessage(ByteString.copyFromUtf8("Tapos check error"))
          .build();
    } catch (TooBigTransactionException e) {
      logger.error("Broadcast transaction {} failed, {}.", trx.getTransactionId(), e.getMessage());
      return builder.setResult(false).setCode(response_code.TOO_BIG_TRANSACTION_ERROR)
          .setMessage(ByteString.copyFromUtf8("transaction size is too big"))
          .build();
    } catch (TransactionExpirationException e) {
      logger.error("Broadcast transaction {} failed, {}.", trx.getTransactionId(), e.getMessage());
      return builder.setResult(false).setCode(response_code.TRANSACTION_EXPIRATION_ERROR)
          .setMessage(ByteString.copyFromUtf8("transaction expired"))
          .build();
    } catch (Exception e) {
      logger.error("Broadcast transaction {} failed, {}.", trx.getTransactionId(), e.getMessage());
      return builder.setResult(false).setCode(response_code.OTHER_ERROR)
          .setMessage(ByteString.copyFromUtf8("other error : " + e.getMessage()))
          .build();
    }
  }

  public TransactionCapsule getTransactionSign(TransactionSign transactionSign) {
    byte[] privateKey = transactionSign.getPrivateKey().toByteArray();
    TransactionCapsule trx = new TransactionCapsule(transactionSign.getTransaction());
    trx.signWithSideChainId(privateKey);
    return trx;
  }

  public TransactionCapsule addSign(TransactionSign transactionSign)
      throws PermissionException, SignatureException, SignatureFormatException {
    byte[] privateKey = transactionSign.getPrivateKey().toByteArray();
    TransactionCapsule trx = new TransactionCapsule(transactionSign.getTransaction());
    trx.addSign(privateKey, dbManager.getAccountStore());
    return trx;
  }

  public TransactionSignWeight getTransactionSignWeight(Transaction trx) {
    TransactionSignWeight.Builder tswBuilder = TransactionSignWeight.newBuilder();
    TransactionExtention.Builder trxExBuilder = TransactionExtention.newBuilder();
    trxExBuilder.setTransaction(trx);
    trxExBuilder.setTxid(ByteString.copyFrom(Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(),
        trx.getRawData().toByteArray())));
    Return.Builder retBuilder = Return.newBuilder();
    retBuilder.setResult(true).setCode(response_code.SUCCESS);
    trxExBuilder.setResult(retBuilder);
    tswBuilder.setTransaction(trxExBuilder);
    Result.Builder resultBuilder = Result.newBuilder();
    try {
      Contract contract = trx.getRawData().getContract(0);
      byte[] owner = TransactionCapsule.getOwner(contract);
      AccountCapsule account = dbManager.getAccountStore().get(owner);
      if (account == null) {
        throw new PermissionException("Account does not exist!");
      }
      int permissionId = contract.getPermissionId();
      Permission permission = account.getPermissionById(permissionId);
      if (permission == null) {
        throw new PermissionException("Permission for this, does not exist!");
      }
      if (permissionId != 0) {
        if (permission.getType() != PermissionType.Active) {
          throw new PermissionException("Permission type is error");
        }
        //check oprations
        if (!checkPermissionOprations(permission, contract)) {
          throw new PermissionException("Permission denied!");
        }
      }
      tswBuilder.setPermission(permission);
      if (trx.getSignatureCount() > 0) {
        List<ByteString> approveList = new ArrayList<ByteString>();
        long currentWeight = TransactionCapsule.checkWeight(permission, trx.getSignatureList(),
            Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(),
                trx.getRawData().toByteArray()), approveList);
        tswBuilder.addAllApprovedList(approveList);
        tswBuilder.setCurrentWeight(currentWeight);
      }
      if (tswBuilder.getCurrentWeight() >= permission.getThreshold()) {
        resultBuilder.setCode(Result.response_code.ENOUGH_PERMISSION);
      } else {
        resultBuilder.setCode(Result.response_code.NOT_ENOUGH_PERMISSION);
      }
    } catch (SignatureFormatException signEx) {
      resultBuilder.setCode(Result.response_code.SIGNATURE_FORMAT_ERROR);
      resultBuilder.setMessage(signEx.getMessage());
    } catch (SignatureException signEx) {
      resultBuilder.setCode(Result.response_code.COMPUTE_ADDRESS_ERROR);
      resultBuilder.setMessage(signEx.getMessage());
    } catch (PermissionException permEx) {
      resultBuilder.setCode(Result.response_code.PERMISSION_ERROR);
      resultBuilder.setMessage(permEx.getMessage());
    } catch (Exception ex) {
      resultBuilder.setCode(Result.response_code.OTHER_ERROR);
      resultBuilder.setMessage(ex.getClass() + " : " + ex.getMessage());
    }
    tswBuilder.setResult(resultBuilder);
    return tswBuilder.build();
  }

  public TransactionApprovedList getTransactionApprovedList(Transaction trx) {
    TransactionApprovedList.Builder tswBuilder = TransactionApprovedList.newBuilder();
    TransactionExtention.Builder trxExBuilder = TransactionExtention.newBuilder();
    trxExBuilder.setTransaction(trx);
    trxExBuilder.setTxid(ByteString.copyFrom(Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(),
        trx.getRawData().toByteArray())));
    Return.Builder retBuilder = Return.newBuilder();
    retBuilder.setResult(true).setCode(response_code.SUCCESS);
    trxExBuilder.setResult(retBuilder);
    tswBuilder.setTransaction(trxExBuilder);
    TransactionApprovedList.Result.Builder resultBuilder = TransactionApprovedList.Result
        .newBuilder();
    try {
      Contract contract = trx.getRawData().getContract(0);
      byte[] owner = TransactionCapsule.getOwner(contract);
      AccountCapsule account = dbManager.getAccountStore().get(owner);
      if (account == null) {
        throw new PermissionException("Account is not exist!");
      }

      if (trx.getSignatureCount() > 0) {
        List<ByteString> approveList = new ArrayList<ByteString>();
        byte[] hash = Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), trx.getRawData().toByteArray());
        for (ByteString sig : trx.getSignatureList()) {
          if (sig.size() < 65) {
            throw new SignatureFormatException(
                "Signature size is " + sig.size());
          }
          String base64 = TransactionCapsule.getBase64FromByteString(sig);
          byte[] address = SignUtils.signatureToAddress(TransactionCapsule.getHashWithSideChainId(hash), base64, Args.getInstance()
              .isECKeyCryptoEngine());
          approveList.add(ByteString.copyFrom(address)); //out put approve list.
        }
        tswBuilder.addAllApprovedList(approveList);
      }
      resultBuilder.setCode(TransactionApprovedList.Result.response_code.SUCCESS);
    } catch (SignatureFormatException signEx) {
      resultBuilder.setCode(TransactionApprovedList.Result.response_code.SIGNATURE_FORMAT_ERROR);
      resultBuilder.setMessage(signEx.getMessage());
    } catch (SignatureException signEx) {
      resultBuilder.setCode(TransactionApprovedList.Result.response_code.COMPUTE_ADDRESS_ERROR);
      resultBuilder.setMessage(signEx.getMessage());
    } catch (Exception ex) {
      resultBuilder.setCode(TransactionApprovedList.Result.response_code.OTHER_ERROR);
      resultBuilder.setMessage(ex.getClass() + " : " + ex.getMessage());
    }
    tswBuilder.setResult(resultBuilder);
    return tswBuilder.build();
  }

  public byte[] pass2Key(byte[] passPhrase) {
    return Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(),
        passPhrase);
  }

  public byte[] createAdresss(byte[] passPhrase) {
    byte[] privateKey = pass2Key(passPhrase);
    SignInterface ecKey = SignUtils.fromPrivate(privateKey,
        Args.getInstance().isECKeyCryptoEngine());
    return ecKey.getAddress();
  }

  public Block getNowBlock() {
    List<BlockCapsule> blockList = dbManager.getBlockStore().getBlockByLatestNum(1);
    if (CollectionUtils.isEmpty(blockList)) {
      return null;
    } else {
      return blockList.get(0).getInstance();
    }
  }

  public Block getBlockByNum(long blockNum) {
    try {
      return dbManager.getBlockByNum(blockNum).getInstance();
    } catch (StoreException e) {
      logger.info(e.getMessage());
      return null;
    }
  }

  public long getTransactionCountByBlockNum(long blockNum) {
    long count = 0;

    try {
      Block block = dbManager.getBlockByNum(blockNum).getInstance();
      count = block.getTransactionsCount();
    } catch (StoreException e) {
      logger.error(e.getMessage());
    }

    return count;
  }

  public WitnessList getWitnessList() {
    WitnessList.Builder builder = WitnessList.newBuilder();
    List<WitnessCapsule> witnessCapsuleList = dbManager.getWitnessStore().getAllWitnesses();
    witnessCapsuleList
        .forEach(witnessCapsule -> builder.addWitnesses(witnessCapsule.getInstance()));
    return builder.build();
  }

  public SideChainProposalList getProposalList() {
    SideChainProposalList.Builder builder = SideChainProposalList.newBuilder();
    List<ProposalCapsule> proposalCapsuleList = dbManager.getProposalStore().getAllProposals();
    proposalCapsuleList
        .forEach(proposalCapsule -> builder.addProposals(proposalCapsule.getInstance()));
    return builder.build();
  }

  public DelegatedResourceList getDelegatedResource(ByteString fromAddress, ByteString toAddress) {
    DelegatedResourceList.Builder builder = DelegatedResourceList.newBuilder();
    byte[] dbKey = DelegatedResourceCapsule
        .createDbKey(fromAddress.toByteArray(), toAddress.toByteArray());
    DelegatedResourceCapsule delegatedResourceCapsule = dbManager.getDelegatedResourceStore()
        .get(dbKey);
    if (delegatedResourceCapsule != null) {
      builder.addDelegatedResource(delegatedResourceCapsule.getInstance());
    }
    return builder.build();
  }

  public DelegatedResourceAccountIndex getDelegatedResourceAccountIndex(ByteString address) {
    DelegatedResourceAccountIndexCapsule accountIndexCapsule =
        dbManager.getDelegatedResourceAccountIndexStore().get(address.toByteArray());
    if (accountIndexCapsule != null) {
      return accountIndexCapsule.getInstance();
    } else {
      return null;
    }
  }

  public ExchangeList getExchangeList() {
    ExchangeList.Builder builder = ExchangeList.newBuilder();
    List<ExchangeCapsule> exchangeCapsuleList = dbManager.getExchangeStoreFinal().getAllExchanges();

    exchangeCapsuleList
        .forEach(exchangeCapsule -> builder.addExchanges(exchangeCapsule.getInstance()));
    return builder.build();
  }

  public Protocol.SideChainParameters getSideChainParameters() {
    Protocol.SideChainParameters.Builder builder = Protocol.SideChainParameters.newBuilder();

    // MAINTENANCE_TIME_INTERVAL, //ms  ,0
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getMaintenanceTimeInterval")
                    .setValue(
                            String.valueOf(dbManager.getDynamicPropertiesStore().getMaintenanceTimeInterval()))
                    .build());
    //    ACCOUNT_UPGRADE_COST, //drop ,1
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getAccountUpgradeCost")
                    .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getAccountUpgradeCost()))
                    .build());
    //    CREATE_ACCOUNT_FEE, //drop ,2
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getCreateAccountFee")
                    .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getCreateAccountFee()))
                    .build());
    //    TRANSACTION_FEE, //drop ,3
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getTransactionFee")
                    .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getTransactionFee()))
                    .build());
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getTransactionSunTokenFee")
                    .setValue(
                            String.valueOf(dbManager.getDynamicPropertiesStore().getTransactionSunTokenFee()))
                    .build());
    //    ASSET_ISSUE_FEE, //drop ,4
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getAssetIssueFee")
                    .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getAssetIssueFee()))
                    .build());
    //    WITNESS_PAY_PER_BLOCK, //drop ,5
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getWitnessPayPerBlock")
                    .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getWitnessPayPerBlock()))
                    .build());
    //    WITNESS_STANDBY_ALLOWANCE, //drop ,6
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getWitnessStandbyAllowance")
                    .setValue(
                            String.valueOf(dbManager.getDynamicPropertiesStore().getWitnessStandbyAllowance()))
                    .build());
    //    CREATE_NEW_ACCOUNT_FEE_IN_SYSTEM_CONTRACT, //drop ,7
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getCreateNewAccountFeeInSystemContract")
                    .setValue(
                            String.valueOf(
                                    dbManager.getDynamicPropertiesStore().getCreateNewAccountFeeInSystemContract()))
                    .build());
    //    CREATE_NEW_ACCOUNT_BANDWIDTH_RATE, // 1 ~ ,8
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getCreateNewAccountBandwidthRate")
                    .setValue(String
                            .valueOf(dbManager.getDynamicPropertiesStore().getCreateNewAccountBandwidthRate()))
                    .build());
    //    ALLOW_CREATION_OF_CONTRACTS, // 0 / >0 ,9
//    builder.addChainParameter(
//        Protocol.SideChainParameters.SideChainParameter.newBuilder()
//            .setKey("getAllowCreationOfContracts")
//            .setValue(dbManager.getDynamicPropertiesStore().getAllowCreationOfContracts())
//            .build());
    //    REMOVE_THE_POWER_OF_THE_GR,  // 1 ,10
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getRemoveThePowerOfTheGr")
                    .setValue(
                            String.valueOf(dbManager.getDynamicPropertiesStore().getRemoveThePowerOfTheGr()))
                    .build());
    //    ENERGY_FEE, // drop, 11
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getEnergyFee")
                    .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getEnergyFee()))
                    .build());
    //    EXCHANGE_CREATE_FEE, // drop, 12
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getExchangeCreateFee")
                    .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getExchangeCreateFee()))
                    .build());
    //    MAX_CPU_TIME_OF_ONE_TX, // ms, 13
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getMaxCpuTimeOfOneTx")
                    .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getMaxCpuTimeOfOneTx()))
                    .build());
    //    ALLOW_UPDATE_ACCOUNT_NAME, // 1, 14
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getAllowUpdateAccountName")
                    .setValue(
                            String.valueOf(dbManager.getDynamicPropertiesStore().getAllowUpdateAccountName()))
                    .build());
    //    ALLOW_SAME_TOKEN_NAME, // 1, 15
//    builder.addChainParameter(
//        Protocol.SideChainParameters.SideChainParameter.newBuilder()
//            .setKey("getAllowSameTokenName")
//            .setValue(dbManager.getDynamicPropertiesStore().getAllowSameTokenName())
//            .build());
    //    ALLOW_DELEGATE_RESOURCE, // 0, 16
//    builder.addChainParameter(
//        Protocol.SideChainParameters.SideChainParameter.newBuilder()
//            .setKey("getAllowDelegateResource")
//            .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getAllowDelegateResource()))
//            .build());
    //    TOTAL_ENERGY_LIMIT, // 50,000,000,000, 17
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getTotalEnergyLimit")
                    .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getTotalEnergyLimit()))
                    .build());
    //    ALLOW_TVM_TRANSFER_TRC10, // 1, 18
//    builder.addChainParameter(
//        Protocol.SideChainParameters.SideChainParameter.newBuilder()
//            .setKey("getAllowTvmTransferTrc10")
//            .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getAllowTvmTransferTrc10()))
//            .build());
    //    TOTAL_CURRENT_ENERGY_LIMIT, // 50,000,000,000, 19
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getTotalEnergyCurrentLimit")
                    .setValue(
                            String.valueOf(dbManager.getDynamicPropertiesStore().getTotalEnergyCurrentLimit()))
                    .build());
    //    ALLOW_MULTI_SIGN, // 1, 20
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getAllowMultiSign")
                    //.setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getAllowMultiSign()))
                    .setValue("1") // should always allowed now.
                    .build());
    //    ALLOW_ADAPTIVE_ENERGY, // 1, 21
    // should keep in side-chain since main-chain not approved yet.
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getAllowAdaptiveEnergy")
                    .setValue(
                            String.valueOf(dbManager.getDynamicPropertiesStore().getAllowAdaptiveEnergy()))
                    .build());
    //other chainParameters
    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getTotalEnergyTargetLimit")
            .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getTotalEnergyTargetLimit()))
            .build());

    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getTotalEnergyAverageUsage")
            .setValue(
                    String.valueOf(dbManager.getDynamicPropertiesStore().getTotalEnergyAverageUsage()))
            .build());

    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getUpdateAccountPermissionFee")
            .setValue(
                    String.valueOf(dbManager.getDynamicPropertiesStore().getUpdateAccountPermissionFee()))
            .build());

    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getMultiSignFee")
            .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getMultiSignFee()))
            .build());

    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getAllowAccountStateRoot")
            .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getAllowAccountStateRoot()))
            .build());

    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getAllowProtoFilterNum")
            .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getAllowProtoFilterNum()))
            .build());

//    // ALLOW_TVM_CONSTANTINOPLE, // 1, 30
//    builder.addChainParameter(
//        Protocol.SideChainParameters.SideChainParameter.newBuilder()
//            .setKey("getAllowTvmConstantinople")
//            .setValue(dbManager.getDynamicPropertiesStore().getAllowTvmConstantinople())
//            .build());

    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getChargingSwitch")
                    .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getChargingSwitch()))
                    .build());

    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getMainChainGateWayList")
            .setValue(convertGateWayListToString(
                    dbManager.getDynamicPropertiesStore().getMainChainGateWayList()))
            .build());

    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getSideChainGateWayList")
            .setValue(convertGateWayListToString(
                    dbManager.getDynamicPropertiesStore().getSideChainGateWayList()))
            .build());
    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getSideChainId")
            .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getSideChainId()))
            .build());
    // PROPOSAL_EXPIRE_TIME, //ms  ,259_200_000L
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getProposalExpireTime")
                    .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getProposalExpireTime()))
                    .build());

    // vote witness switch
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getVoteWitnessSwitch")
                    .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getVoteWitnessSwitch()))
                    .build());
    // side chain charging bandwidth
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getSideChainChargingBandwidth")
                    .setValue(String
                            .valueOf(dbManager.getDynamicPropertiesStore().getSideChainChargingBandwidth()))
                    .build());

    // Fund
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getFund")
                    .setValue(String
                            .valueOf(dbManager.getDynamicPropertiesStore().getFund()))
                    .build());

    // Founder
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getFundInjectAddress")
                    .setValue(WalletUtil.encode58Check(dbManager.getDynamicPropertiesStore().getFundInjectAddress()))
                    .build());

    // FundDistributeEnableSwitch 0,1
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getFundDistributeEnableSwitch")
                    .setValue(String
                            .valueOf(dbManager.getDynamicPropertiesStore().getFundDistributeEnableSwitch()))
                    .build());

    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getDayToSustainByFund")
                    .setValue(String
                            .valueOf(dbManager.getDynamicPropertiesStore().getDayToSustainByFund()))
                    .build());
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getPercentToPayWitness")
                    .setValue(String
                            .valueOf(dbManager.getDynamicPropertiesStore().getPercentToPayWitness()))
                    .build());

    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getWitnessMaxActiveNum")
                    .setValue(String
                            .valueOf(dbManager.getDynamicPropertiesStore().getWitnessMaxActiveNum()))
                    .build());
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getUpdateGateway_v1_0_2")
                    .setValue(String
                            .valueOf(dbManager.getDynamicPropertiesStore().getAllowUpdateGatewayV102()))
                    .build());
    builder.addChainParameter(
        Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getAllowDapp_v1_0_5")
            .setValue(String
                .valueOf(dbManager.getDynamicPropertiesStore().getAllowDappV152()))
            .build());
    builder.addChainParameter(
            Protocol.SideChainParameters.SideChainParameter.newBuilder()
                    .setKey("getAllowTvmSolidity059")
                    .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getAllowTvmSolidity059()))
                    .build());
    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getForbidTransferToContract")
            .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getForbidTransferToContract()))
            .build());

    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getAdaptiveResourceLimitTargetRatio")
            .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getAdaptiveResourceLimitTargetRatio() / (24 * 60)))
            .build());

    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getAdaptiveResourceLimitMultiplier")
            .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getAdaptiveResourceLimitMultiplier()))
            .build());

//    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
//            .setKey("getChangeDelegation")
//            .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getChangeDelegation()))
//            .build());

    builder.addChainParameter(Protocol.SideChainParameters.SideChainParameter.newBuilder()
            .setKey("getWitness127PayPerBlock")
            .setValue(String.valueOf(dbManager.getDynamicPropertiesStore().getWitness127PayPerBlock()))
            .build());

    return builder.build();
  }

  public AssetIssueList getAssetIssueList() {
    AssetIssueList.Builder builder = AssetIssueList.newBuilder();

    dbManager.getAssetIssueStoreFinal().getAllAssetIssues()
        .forEach(issueCapsule -> builder.addAssetIssue(issueCapsule.getInstance()));

    return builder.build();
  }

  public AssetIssueList getAssetIssueList(long offset, long limit) {
    AssetIssueList.Builder builder = AssetIssueList.newBuilder();

    List<AssetIssueCapsule> assetIssueList =
        dbManager.getAssetIssueStoreFinal().getAssetIssuesPaginated(offset, limit);

    if (CollectionUtils.isEmpty(assetIssueList)) {
      return null;
    }

    assetIssueList.forEach(issueCapsule -> builder.addAssetIssue(issueCapsule.getInstance()));
    return builder.build();
  }

  public AssetIssueList getAssetIssueByAccount(ByteString accountAddress) {
    if (accountAddress == null || accountAddress.isEmpty()) {
      return null;
    }

    List<AssetIssueCapsule> assetIssueCapsuleList =
        dbManager.getAssetIssueStoreFinal().getAllAssetIssues();

    AssetIssueList.Builder builder = AssetIssueList.newBuilder();
    assetIssueCapsuleList.stream()
        .filter(assetIssueCapsule -> assetIssueCapsule.getOwnerAddress().equals(accountAddress))
        .forEach(issueCapsule -> {
          builder.addAssetIssue(issueCapsule.getInstance());
        });

    return builder.build();
  }

  public AccountNetMessage getAccountNet(ByteString accountAddress) {
    if (accountAddress == null || accountAddress.isEmpty()) {
      return null;
    }
    AccountNetMessage.Builder builder = AccountNetMessage.newBuilder();
    AccountCapsule accountCapsule = dbManager.getAccountStore().get(accountAddress.toByteArray());
    if (accountCapsule == null) {
      return null;
    }

    BandwidthProcessor processor = new BandwidthProcessor(dbManager);
    processor.updateUsage(accountCapsule);

    long netLimit = processor
        .calculateGlobalNetLimit(accountCapsule);
    long freeNetLimit = dbManager.getDynamicPropertiesStore().getFreeNetLimit();
    long totalNetLimit = dbManager.getDynamicPropertiesStore().getTotalNetLimit();
    long totalNetWeight = dbManager.getDynamicPropertiesStore().getTotalNetWeight();

    Map<String, Long> assetNetLimitMap = new HashMap<>();
//    Map<String, Long> allFreeAssetNetUsage;
//    if (dbManager.getDynamicPropertiesStore().getAllowSameTokenName() == 0) {
//      allFreeAssetNetUsage = accountCapsule.getAllFreeAssetNetUsage();
//      allFreeAssetNetUsage.keySet().forEach(asset -> {
//        byte[] key = ByteArray.fromString(asset);
//        assetNetLimitMap
//            .put(asset, dbManager.getAssetIssueStore().get(key).getFreeAssetNetLimit());
//      });
//    } else {
//      allFreeAssetNetUsage = accountCapsule.getAllFreeAssetNetUsageV2();
//      allFreeAssetNetUsage.keySet().forEach(asset -> {
//        byte[] key = ByteArray.fromString(asset);
//        assetNetLimitMap
//            .put(asset, dbManager.getAssetIssueV2Store().get(key).getFreeAssetNetLimit());
//      });
//    }

    builder.setFreeNetUsed(accountCapsule.getFreeNetUsage())
        .setFreeNetLimit(freeNetLimit)
        .setNetUsed(accountCapsule.getNetUsage())
        .setNetLimit(netLimit)
        .setTotalNetLimit(totalNetLimit)
        .setTotalNetWeight(totalNetWeight);
    return builder.build();
  }

  public AccountResourceMessage getAccountResource(ByteString accountAddress) {
    if (accountAddress == null || accountAddress.isEmpty()) {
      return null;
    }
    AccountResourceMessage.Builder builder = AccountResourceMessage.newBuilder();
    AccountCapsule accountCapsule = dbManager.getAccountStore().get(accountAddress.toByteArray());
    if (accountCapsule == null) {
      return null;
    }

    BandwidthProcessor processor = new BandwidthProcessor(dbManager);
    processor.updateUsage(accountCapsule);

    EnergyProcessor energyProcessor = new EnergyProcessor(dbManager.getDynamicPropertiesStore(),
        dbManager.getAccountStore());
    energyProcessor.updateUsage(accountCapsule);

    long netLimit = processor
        .calculateGlobalNetLimit(accountCapsule);
    long freeNetLimit = dbManager.getDynamicPropertiesStore().getFreeNetLimit();
    long totalNetLimit = dbManager.getDynamicPropertiesStore().getTotalNetLimit();
    long totalNetWeight = dbManager.getDynamicPropertiesStore().getTotalNetWeight();
    long energyLimit = energyProcessor
        .calculateGlobalEnergyLimit(accountCapsule);
    long totalEnergyLimit = dbManager.getDynamicPropertiesStore().getTotalEnergyCurrentLimit();
    long totalEnergyWeight = dbManager.getDynamicPropertiesStore().getTotalEnergyWeight();

    long storageLimit = accountCapsule.getAccountResource().getStorageLimit();
    long storageUsage = accountCapsule.getAccountResource().getStorageUsage();

    Map<String, Long> assetNetLimitMap = new HashMap<>();
//    Map<String, Long> allFreeAssetNetUsage;
//    if (dbManager.getDynamicPropertiesStore().getAllowSameTokenName() == 0) {
//      allFreeAssetNetUsage = accountCapsule.getAllFreeAssetNetUsage();
//      allFreeAssetNetUsage.keySet().forEach(asset -> {
//        byte[] key = ByteArray.fromString(asset);
//        assetNetLimitMap
//            .put(asset, dbManager.getAssetIssueStore().get(key).getFreeAssetNetLimit());
//      });
//    } else {
//      allFreeAssetNetUsage = accountCapsule.getAllFreeAssetNetUsageV2();
//      allFreeAssetNetUsage.keySet().forEach(asset -> {
//        byte[] key = ByteArray.fromString(asset);
//        assetNetLimitMap
//            .put(asset, dbManager.getAssetIssueV2Store().get(key).getFreeAssetNetLimit());
//      });
//    }

    builder.setFreeNetUsed(accountCapsule.getFreeNetUsage())
        .setFreeNetLimit(freeNetLimit)
        .setNetUsed(accountCapsule.getNetUsage())
        .setNetLimit(netLimit)
        .setTotalNetLimit(totalNetLimit)
        .setTotalNetWeight(totalNetWeight)
        .setEnergyLimit(energyLimit)
        .setEnergyUsed(accountCapsule.getAccountResource().getEnergyUsage())
        .setTotalEnergyLimit(totalEnergyLimit)
        .setTotalEnergyWeight(totalEnergyWeight)
        .setStorageLimit(storageLimit)
        .setStorageUsed(storageUsage);
    return builder.build();
  }

  public AssetIssueContract getAssetIssueByName(ByteString assetName)
      throws NonUniqueObjectException {
    if (assetName == null || assetName.isEmpty()) {
      return null;
    }

    // get asset issue by name from new DB
    List<AssetIssueCapsule> assetIssueCapsuleList =
        dbManager.getAssetIssueV2Store().getAllAssetIssues();
    AssetIssueList.Builder builder = AssetIssueList.newBuilder();
    assetIssueCapsuleList
        .stream()
        .filter(assetIssueCapsule -> assetIssueCapsule.getName().equals(assetName))
        .forEach(
            issueCapsule -> {
              builder.addAssetIssue(issueCapsule.getInstance());
            });

    // check count
    if (builder.getAssetIssueCount() > 1) {
      throw new NonUniqueObjectException(
          "To get more than one asset, please use getAssetIssuebyid syntax");
    } else {
      // fetch from DB by assetName as id
      AssetIssueCapsule assetIssueCapsule =
          dbManager.getAssetIssueV2Store().get(assetName.toByteArray());

      if (assetIssueCapsule != null) {
        // check already fetch
        if (builder.getAssetIssueCount() > 0
            && builder.getAssetIssue(0).getId().equals(assetIssueCapsule.getInstance().getId())) {
          return assetIssueCapsule.getInstance();
        }

        builder.addAssetIssue(assetIssueCapsule.getInstance());
        // check count
        if (builder.getAssetIssueCount() > 1) {
          throw new NonUniqueObjectException(
              "To get more than one asset, please use getAssetIssuebyid syntax");
        }
      }
    }

    if (builder.getAssetIssueCount() > 0) {
      return builder.getAssetIssue(0);
    } else {
      return null;
    }
  }

  public AssetIssueList getAssetIssueListByName(ByteString assetName) {
    if (assetName == null || assetName.isEmpty()) {
      return null;
    }

    List<AssetIssueCapsule> assetIssueCapsuleList =
        dbManager.getAssetIssueStoreFinal().getAllAssetIssues();

    AssetIssueList.Builder builder = AssetIssueList.newBuilder();
    assetIssueCapsuleList.stream()
        .filter(assetIssueCapsule -> assetIssueCapsule.getName().equals(assetName))
        .forEach(issueCapsule -> {
          builder.addAssetIssue(issueCapsule.getInstance());
        });

    return builder.build();
  }

  public AssetIssueContract getAssetIssueById(String assetId) {
    if (assetId == null || assetId.isEmpty()) {
      return null;
    }
    AssetIssueCapsule assetIssueCapsule = dbManager.getAssetIssueV2Store()
        .get(ByteArray.fromString(assetId));
    return assetIssueCapsule != null ? assetIssueCapsule.getInstance() : null;
  }

  public NumberMessage totalTransaction() {
    NumberMessage.Builder builder = NumberMessage.newBuilder()
        .setNum(dbManager.getTransactionStore().getTotalTransactions());
    return builder.build();
  }

  public NumberMessage getNextMaintenanceTime() {
    NumberMessage.Builder builder = NumberMessage.newBuilder()
        .setNum(dbManager.getDynamicPropertiesStore().getNextMaintenanceTime());
    return builder.build();
  }

  public Block getBlockById(ByteString blockId) {
    if (Objects.isNull(blockId)) {
      return null;
    }
    Block block = null;
    try {
      block = dbManager.getBlockStore().get(blockId.toByteArray()).getInstance();
    } catch (StoreException e) {
    }
    return block;
  }

  public BlockList getBlocksByLimitNext(long number, long limit) {
    if (limit <= 0) {
      return null;
    }
    BlockList.Builder blockListBuilder = BlockList.newBuilder();
    dbManager.getBlockStore().getLimitNumber(number, limit).forEach(
        blockCapsule -> blockListBuilder.addBlock(blockCapsule.getInstance()));
    return blockListBuilder.build();
  }

  public BlockList getBlockByLatestNum(long getNum) {
    BlockList.Builder blockListBuilder = BlockList.newBuilder();
    dbManager.getBlockStore().getBlockByLatestNum(getNum).forEach(
        blockCapsule -> blockListBuilder.addBlock(blockCapsule.getInstance()));
    return blockListBuilder.build();
  }

  public Transaction getTransactionById(ByteString transactionId) {
    if (Objects.isNull(transactionId)) {
      return null;
    }
    TransactionCapsule transactionCapsule = null;
    try {
      transactionCapsule = dbManager.getTransactionStore()
          .get(transactionId.toByteArray());
    } catch (StoreException e) {
      return null;
    }
    if (transactionCapsule != null) {
      return transactionCapsule.getInstance();
    }
    return null;
  }

  public TransactionInfo getTransactionInfoById(ByteString transactionId) {
    if (Objects.isNull(transactionId)) {
      return null;
    }
    TransactionInfoCapsule transactionInfoCapsule;
    try {
      transactionInfoCapsule = dbManager.getTransactionHistoryStore()
          .get(transactionId.toByteArray());
    } catch (StoreException e) {
      return null;
    }
    if (transactionInfoCapsule != null) {
      return transactionInfoCapsule.getInstance();
    }
    try {
      transactionInfoCapsule = dbManager.getTransactionRetStore()
          .getTransactionInfo(transactionId.toByteArray());
    } catch (BadItemException e) {
      return null;
    }

    return transactionInfoCapsule == null ? null : transactionInfoCapsule.getInstance();
  }

  public SideChainProposal getProposalById(ByteString proposalId) {
    if (Objects.isNull(proposalId)) {
      return null;
    }
    ProposalCapsule proposalCapsule = null;
    try {
      proposalCapsule = dbManager.getProposalStore()
          .get(proposalId.toByteArray());
    } catch (StoreException e) {
      return null;
    }
    if (proposalCapsule != null) {
      return proposalCapsule.getInstance();
    }
    return null;
  }

  public Exchange getExchangeById(ByteString exchangeId) {
    if (Objects.isNull(exchangeId)) {
      return null;
    }
    ExchangeCapsule exchangeCapsule = null;
    try {
      exchangeCapsule = dbManager.getExchangeStoreFinal().get(exchangeId.toByteArray());
    } catch (StoreException e) {
    }
    if (exchangeCapsule != null) {
      return exchangeCapsule.getInstance();
    }
    return null;
  }

  public boolean getFullNodeAllowShieldedTransaction() {
    return Args.getInstance().isFullNodeAllowShieldedTransactionArgs();
  }

  public BytesMessage getNullifier(ByteString id) {
    if (Objects.isNull(id)) {
      return null;
    }
    BytesCapsule nullifier = null;
    nullifier = dbManager.getNullfierStore().get(id.toByteArray());

    if (nullifier != null) {
      return BytesMessage.newBuilder().setValue(ByteString.copyFrom(nullifier.getData())).build();
    }
    return null;
  }

  private long getBlockNumber(OutputPoint outPoint)
      throws BadItemException, ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    ByteString txId = outPoint.getHash();

    long blockNum = dbManager.getTransactionStore().getBlockNumber(txId.toByteArray());
    if (blockNum <= 0) {
      throw new RuntimeException("tx is not found:" + ByteArray.toHexString(txId.toByteArray()));
    }

    return blockNum;
  }

  //in:outPoint,out:blockNumber
  private IncrementalMerkleVoucherContainer createWitness(OutputPoint outPoint, Long blockNumber)
      throws ItemNotFoundException, BadItemException, InvalidProtocolBufferException, ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    ByteString txId = outPoint.getHash();

    //Get the tree in blockNum-1 position
    byte[] treeRoot = dbManager.getMerkleTreeIndexStore().get(blockNumber - 1);
    if (treeRoot == null) {
      throw new RuntimeException("treeRoot is null,blockNumber:" + (blockNumber - 1));
    }

    IncrementalMerkleTreeCapsule treeCapsule = dbManager.getMerkleTreeStore()
        .get(treeRoot);
    if (treeCapsule == null) {
      if ("fbc2f4300c01f0b7820d00e3347c8da4ee614674376cbc45359daa54f9b5493e"
          .equals(ByteArray.toHexString(treeRoot))) {
        treeCapsule = new IncrementalMerkleTreeCapsule();
      } else {
        throw new RuntimeException("tree is null,treeRoot:" + ByteArray.toHexString(treeRoot));
      }

    }
    IncrementalMerkleTreeContainer tree = treeCapsule.toMerkleTreeContainer();

    //Get the block of blockNum
    BlockCapsule block = dbManager.getBlockByNum(blockNumber);

    IncrementalMerkleVoucherContainer witness = null;

    //get the witness in three parts
    boolean found = false;
    for (Transaction transaction : block.getInstance().getTransactionsList()) {

      Contract contract = transaction.getRawData().getContract(0);
      if (contract.getType() == ContractType.ShieldedTransferContract) {
        ShieldedTransferContract zkContract = contract.getParameter()
            .unpack(ShieldedTransferContract.class);

        if (new TransactionCapsule(transaction).getTransactionId().getByteString().equals(txId)) {
          found = true;

          if (outPoint.getIndex() >= zkContract.getReceiveDescriptionCount()) {
            throw new RuntimeException("outPoint.getIndex():" + outPoint.getIndex()
                + " >= zkContract.getReceiveDescriptionCount():" + zkContract
                .getReceiveDescriptionCount());
          }

          int index = 0;
          for (ReceiveDescription receiveDescription : zkContract.getReceiveDescriptionList()) {
            PedersenHashCapsule cmCapsule = new PedersenHashCapsule();
            cmCapsule.setContent(receiveDescription.getNoteCommitment());
            PedersenHash cm = cmCapsule.getInstance();

            if (index < outPoint.getIndex()) {
              tree.append(cm);
            } else if (outPoint.getIndex() == index) {
              tree.append(cm);
              witness = tree.getTreeCapsule().deepCopy()
                  .toMerkleTreeContainer().toVoucher();
            } else {
              if (witness != null) {
                witness.append(cm);
              } else {
                throw new ZksnarkException("witness is null!");
              }
            }

            index++;
          }

        } else {
          for (ReceiveDescription receiveDescription :
              zkContract.getReceiveDescriptionList()) {
            PedersenHashCapsule cmCapsule = new PedersenHashCapsule();
            cmCapsule.setContent(receiveDescription.getNoteCommitment());
            PedersenHash cm = cmCapsule.getInstance();
            if (witness != null) {
              witness.append(cm);
            } else {
              tree.append(cm);
            }

          }
        }
      }
    }

    if (!found) {
      throw new RuntimeException("not found cm");
    }

    return witness;

  }

  private void updateWitnesses(List<IncrementalMerkleVoucherContainer> witnessList, long large,
      int synBlockNum) throws ItemNotFoundException, BadItemException,
      InvalidProtocolBufferException, ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    long start = large;
    long end = large + synBlockNum - 1;

    long latestBlockHeaderNumber = dbManager.getDynamicPropertiesStore()
        .getLatestBlockHeaderNumber();

    if (end > latestBlockHeaderNumber) {
      throw new RuntimeException(
          "synBlockNum is too large, cmBlockNum plus synBlockNum must be <= latestBlockNumber");
    }

    for (long n = start; n <= end; n++) {
      BlockCapsule block = dbManager.getBlockByNum(n);
      for (Transaction transaction1 : block.getInstance().getTransactionsList()) {

        Contract contract1 = transaction1.getRawData().getContract(0);
        if (contract1.getType() == ContractType.ShieldedTransferContract) {

          ShieldedTransferContract zkContract = contract1.getParameter()
              .unpack(ShieldedTransferContract.class);

          for (ReceiveDescription receiveDescription :
              zkContract.getReceiveDescriptionList()) {

            PedersenHashCapsule cmCapsule = new PedersenHashCapsule();
            cmCapsule.setContent(receiveDescription.getNoteCommitment());
            PedersenHash cm = cmCapsule.getInstance();
            for (IncrementalMerkleVoucherContainer wit : witnessList) {
              wit.append(cm);
            }
          }

        }
      }
    }
  }

  private void updateLowWitness(IncrementalMerkleVoucherContainer witness, long blockNum1,
      long blockNum2) throws ItemNotFoundException, BadItemException,
      InvalidProtocolBufferException, ZksnarkException {
    long start;
    long end;
    if (blockNum1 < blockNum2) {
      start = blockNum1 + 1;
      end = blockNum2;
    } else {
      return;
    }

    for (long n = start; n <= end; n++) {
      BlockCapsule block = dbManager.getBlockByNum(n);
      for (Transaction transaction1 : block.getInstance().getTransactionsList()) {

        Contract contract1 = transaction1.getRawData().getContract(0);
        if (contract1.getType() == ContractType.ShieldedTransferContract) {

          ShieldedTransferContract zkContract = contract1.getParameter()
              .unpack(ShieldedTransferContract.class);

          for (ReceiveDescription receiveDescription :
              zkContract.getReceiveDescriptionList()) {

            PedersenHashCapsule cmCapsule = new PedersenHashCapsule();
            cmCapsule.setContent(receiveDescription.getNoteCommitment());
            PedersenHash cm = cmCapsule.getInstance();
            witness.append(cm);
          }

        }
      }
    }
  }

  private void validateInput(OutputPointInfo request) throws BadItemException, ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    if (request.getBlockNum() < 0 || request.getBlockNum() > 1000) {
      throw new BadItemException("request.BlockNum must be specified with range in【0，1000】");
    }

    if (request.getOutPointsCount() < 1 || request.getOutPointsCount() > 10) {
      throw new BadItemException("request.OutPointsCount must be speccified with range in【1，10】");
    }

    for (OutputPoint outputPoint : request.getOutPointsList()) {

      if (outputPoint.getHash() == null) {
        throw new BadItemException("outPoint.getHash() == null");
      }
      if (outputPoint.getIndex() >= Constant.ZC_OUTPUT_DESC_MAX_SIZE
          || outputPoint.getIndex() < 0) {
        throw new BadItemException(
            "outPoint.getIndex() > " + Constant.ZC_OUTPUT_DESC_MAX_SIZE
                + " || outPoint.getIndex() < 0");
      }
    }
  }

  public IncrementalMerkleVoucherInfo getMerkleTreeVoucherInfo(OutputPointInfo request)
      throws ItemNotFoundException, BadItemException,
      InvalidProtocolBufferException, ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    validateInput(request);
    IncrementalMerkleVoucherInfo.Builder result = IncrementalMerkleVoucherInfo.newBuilder();

    long largeBlockNum = 0;
    for (OutputPoint outputPoint : request.getOutPointsList()) {
      Long blockNum1 = getBlockNumber(outputPoint);
      if (blockNum1 > largeBlockNum) {
        largeBlockNum = blockNum1;
      }
    }

    logger.debug("largeBlockNum:" + largeBlockNum);
    int opIndex = 0;

    List<IncrementalMerkleVoucherContainer> witnessList = Lists.newArrayList();
    for (OutputPoint outputPoint : request.getOutPointsList()) {
      Long blockNum1 = getBlockNumber(outputPoint);
      logger.debug("blockNum:" + blockNum1 + ",opIndex:" + opIndex++);
      if (blockNum1 + 100 < largeBlockNum) {
        throw new RuntimeException(
            "blockNum:" + blockNum1 + " + 100 < largeBlockNum:" + largeBlockNum);
      }
      IncrementalMerkleVoucherContainer witness = createWitness(outputPoint, blockNum1);
      updateLowWitness(witness, blockNum1, largeBlockNum);
      witnessList.add(witness);
    }

    int synBlockNum = request.getBlockNum();
    if (synBlockNum != 0) {
      //According to the blockNum in the request, obtain the block before [block2+1, blockNum], and update the two witnesses.
      updateWitnesses(witnessList, largeBlockNum + 1, synBlockNum);
    }

    for (IncrementalMerkleVoucherContainer w : witnessList) {
      w.getVoucherCapsule().resetRt();
      result.addVouchers(w.getVoucherCapsule().getInstance());
      result.addPaths(ByteString.copyFrom(w.path().encode()));
    }

    return result.build();
  }

  public IncrementalMerkleTree getMerkleTreeOfBlock(long blockNum) throws ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    if (blockNum < 0) {
      return null;
    }

    try {
      if (dbManager.getMerkleTreeIndexStore().has(ByteArray.fromLong(blockNum))) {
        return IncrementalMerkleTree
            .parseFrom(dbManager.getMerkleTreeIndexStore().get(blockNum));
      }
    } catch (Exception ex) {
      return null;
    }
    return null;
  }

  public long getShieldedTransactionFee() {
    return dbManager.getDynamicPropertiesStore().getShieldedTransactionFee();
  }

  public void checkCmValid(List<SpendNote> shieldedSpends, List<ReceiveNote> shieldedReceives)
      throws ContractValidateException {
    checkCmNumber(shieldedSpends, shieldedReceives);
    checkCmValue(shieldedSpends, shieldedReceives);
  }

  public void checkCmNumber(List<SpendNote> shieldedSpends, List<ReceiveNote> shieldedReceives)
      throws ContractValidateException {
    if (!shieldedSpends.isEmpty() && shieldedSpends.size() > 1) {
      throw new ContractValidateException("The number of spend note must <=1");
    }

    if (!shieldedReceives.isEmpty() && shieldedReceives.size() > 2) {
      throw new ContractValidateException("The number of receive note must <=2");
    }
  }

  public void checkCmValue(List<SpendNote> shieldedSpends, List<ReceiveNote> shieldedReceives)
      throws ContractValidateException {
    for (SpendNote spendNote : shieldedSpends) {
      if (spendNote.getNote().getValue() < 0) {
        throw new ContractValidateException("The value in SpendNote must >= 0");
      }
    }

    for (ReceiveNote receiveNote : shieldedReceives) {
      if (receiveNote.getNote().getValue() < 0) {
        throw new ContractValidateException("The value in ReceiveNote must >= 0");
      }
    }
  }

  public ReceiveNote createReceiveNoteRandom(long value) throws ZksnarkException, BadItemException {
    SpendingKey spendingKey = SpendingKey.random();
    PaymentAddress paymentAddress = spendingKey.defaultAddress();

    GrpcAPI.Note note = GrpcAPI.Note.newBuilder().setValue(value)
        .setPaymentAddress(KeyIo.encodePaymentAddress(paymentAddress))
        .setRcm(ByteString.copyFrom(Note.generateR()))
        .setMemo(ByteString.copyFrom(new byte[512])).build();

    return ReceiveNote.newBuilder().setNote(note).build();
  }

  public TransactionCapsule createShieldedTransaction(PrivateParameters request)
      throws ContractValidateException, RuntimeException, ZksnarkException, BadItemException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException("ShieldedTransactionApi is not allowed");
    }
    ZenTransactionBuilder builder = new ZenTransactionBuilder(this);

    byte[] transparentFromAddress = request.getTransparentFromAddress().toByteArray();
    byte[] ask = request.getAsk().toByteArray();
    byte[] nsk = request.getNsk().toByteArray();
    byte[] ovk = request.getOvk().toByteArray();

    if (ArrayUtils.isEmpty(transparentFromAddress) && (ArrayUtils.isEmpty(ask) || ArrayUtils
        .isEmpty(nsk) || ArrayUtils.isEmpty(ovk))) {
      throw new ContractValidateException("No input address");
    }

    long fromAmount = request.getFromAmount();
    if (!ArrayUtils.isEmpty(transparentFromAddress) && fromAmount <= 0) {
      throw new ContractValidateException("Input amount must > 0");
    }

    List<SpendNote> shieldedSpends = request.getShieldedSpendsList();
    if (!(ArrayUtils.isEmpty(ask) || ArrayUtils.isEmpty(nsk) || ArrayUtils.isEmpty(ovk))
        && shieldedSpends.isEmpty()) {
      throw new ContractValidateException("No input note");
    }

    List<ReceiveNote> shieldedReceives = request.getShieldedReceivesList();
    byte[] transparentToAddress = request.getTransparentToAddress().toByteArray();
    if (shieldedReceives.isEmpty() && ArrayUtils.isEmpty(transparentToAddress)) {
      throw new ContractValidateException("No output address");
    }

    long toAmount = request.getToAmount();
    if (!ArrayUtils.isEmpty(transparentToAddress) && toAmount <= 0) {
      throw new ContractValidateException("Output amount must > 0");
    }

    checkCmValid(shieldedSpends, shieldedReceives);

    // add
    if (!ArrayUtils.isEmpty(transparentFromAddress)) {
      builder.setTransparentInput(transparentFromAddress, fromAmount);
    }

    if (!ArrayUtils.isEmpty(transparentToAddress)) {
      builder.setTransparentOutput(transparentToAddress, toAmount);
    }

    // from shielded to public, without shielded receive, will create a random shielded address
    if (!shieldedSpends.isEmpty()
        && !ArrayUtils.isEmpty(transparentToAddress)
        && shieldedReceives.isEmpty()) {
      shieldedReceives = new ArrayList<>();
      ReceiveNote receiveNote = createReceiveNoteRandom(0);
      shieldedReceives.add(receiveNote);
    }

    // input
    if (!(ArrayUtils.isEmpty(ask) || ArrayUtils.isEmpty(nsk) || ArrayUtils.isEmpty(ovk))) {
      ExpandedSpendingKey expsk = new ExpandedSpendingKey(ask, nsk, ovk);
      for (SpendNote spendNote : shieldedSpends) {
        GrpcAPI.Note note = spendNote.getNote();
        PaymentAddress paymentAddress = KeyIo.decodePaymentAddress(note.getPaymentAddress());
        if (paymentAddress == null) {
          throw new ZksnarkException(PAYMENT_ADDRESS_FORMAT_WRONG);
        }
        Note baseNote = new Note(paymentAddress.getD(),
            paymentAddress.getPkD(), note.getValue(), note.getRcm().toByteArray());

        IncrementalMerkleVoucherContainer voucherContainer = new IncrementalMerkleVoucherCapsule(
            spendNote.getVoucher()).toMerkleVoucherContainer();
        builder.addSpend(expsk,
            baseNote,
            spendNote.getAlpha().toByteArray(),
            spendNote.getVoucher().getRt().toByteArray(),
            voucherContainer);
      }
    }

    // output
    for (ReceiveNote receiveNote : shieldedReceives) {
      PaymentAddress paymentAddress = KeyIo.decodePaymentAddress(
          receiveNote.getNote().getPaymentAddress());
      if (paymentAddress == null) {
        throw new ZksnarkException(PAYMENT_ADDRESS_FORMAT_WRONG);
      }
      builder.addOutput(ovk, paymentAddress.getD(), paymentAddress.getPkD(),
          receiveNote.getNote().getValue(), receiveNote.getNote().getRcm().toByteArray(),
          receiveNote.getNote().getMemo().toByteArray());
    }

    TransactionCapsule transactionCapsule = null;
    try {
      transactionCapsule = builder.build();
    } catch (ZksnarkException e) {
      logger.error("createShieldedTransaction except, error is " + e.toString());
      throw new ZksnarkException(e.toString());
    }
    return transactionCapsule;

  }

  public TransactionCapsule createShieldedTransactionWithoutSpendAuthSig(
      PrivateParametersWithoutAsk request)
      throws ContractValidateException, ZksnarkException, BadItemException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }

    ZenTransactionBuilder builder = new ZenTransactionBuilder(this);

    byte[] transparentFromAddress = request.getTransparentFromAddress().toByteArray();
    byte[] ak = request.getAk().toByteArray();
    byte[] nsk = request.getNsk().toByteArray();
    byte[] ovk = request.getOvk().toByteArray();

    if (ArrayUtils.isEmpty(transparentFromAddress) && (ArrayUtils.isEmpty(ak) || ArrayUtils
        .isEmpty(nsk) || ArrayUtils.isEmpty(ovk))) {
      throw new ContractValidateException("No input address");
    }

    long fromAmount = request.getFromAmount();
    if (!ArrayUtils.isEmpty(transparentFromAddress) && fromAmount <= 0) {
      throw new ContractValidateException("Input amount must > 0");
    }

    List<SpendNote> shieldedSpends = request.getShieldedSpendsList();
    if (!(ArrayUtils.isEmpty(ak) || ArrayUtils.isEmpty(nsk) || ArrayUtils.isEmpty(ovk))
        && shieldedSpends.isEmpty()) {
      throw new ContractValidateException("No input note");
    }

    List<ReceiveNote> shieldedReceives = request.getShieldedReceivesList();
    byte[] transparentToAddress = request.getTransparentToAddress().toByteArray();
    if (shieldedReceives.isEmpty() && ArrayUtils.isEmpty(transparentToAddress)) {
      throw new ContractValidateException("No output address");
    }

    long toAmount = request.getToAmount();
    if (!ArrayUtils.isEmpty(transparentToAddress) && toAmount <= 0) {
      throw new ContractValidateException("Output amount must > 0");
    }

    checkCmValid(shieldedSpends, shieldedReceives);

    // add
    if (!ArrayUtils.isEmpty(transparentFromAddress)) {
      builder.setTransparentInput(transparentFromAddress, fromAmount);
    }

    if (!ArrayUtils.isEmpty(transparentToAddress)) {
      builder.setTransparentOutput(transparentToAddress, toAmount);
    }

    // from shielded to public, without shielded receive, will create a random shielded address
    if (!shieldedSpends.isEmpty()
        && !ArrayUtils.isEmpty(transparentToAddress)
        && shieldedReceives.isEmpty()) {
      shieldedReceives = new ArrayList<>();
      ReceiveNote receiveNote = createReceiveNoteRandom(0);
      shieldedReceives.add(receiveNote);
    }

    // input
    if (!(ArrayUtils.isEmpty(ak) || ArrayUtils.isEmpty(nsk) || ArrayUtils.isEmpty(ovk))) {
      for (SpendNote spendNote : shieldedSpends) {
        GrpcAPI.Note note = spendNote.getNote();
        PaymentAddress paymentAddress = KeyIo.decodePaymentAddress(note.getPaymentAddress());
        if (paymentAddress == null) {
          throw new ZksnarkException(PAYMENT_ADDRESS_FORMAT_WRONG);
        }
        Note baseNote = new Note(paymentAddress.getD(),
            paymentAddress.getPkD(), note.getValue(), note.getRcm().toByteArray());

        IncrementalMerkleVoucherContainer voucherContainer = new IncrementalMerkleVoucherCapsule(
            spendNote.getVoucher()).toMerkleVoucherContainer();
        builder.addSpend(ak,
            nsk,
            ovk,
            baseNote,
            spendNote.getAlpha().toByteArray(),
            spendNote.getVoucher().getRt().toByteArray(),
            voucherContainer);
      }
    }

    // output
    for (ReceiveNote receiveNote : shieldedReceives) {
      PaymentAddress paymentAddress = KeyIo.decodePaymentAddress(
          receiveNote.getNote().getPaymentAddress());
      if (paymentAddress == null) {
        throw new ZksnarkException(PAYMENT_ADDRESS_FORMAT_WRONG);
      }
      builder.addOutput(ovk, paymentAddress.getD(), paymentAddress.getPkD(),
          receiveNote.getNote().getValue(), receiveNote.getNote().getRcm().toByteArray(),
          receiveNote.getNote().getMemo().toByteArray());
    }

    TransactionCapsule transactionCapsule = null;
    try {
      transactionCapsule = builder.buildWithoutAsk();
    } catch (ZksnarkException e) {
      logger.error("createShieldedTransaction except, error is " + e.toString());
      throw new ZksnarkException(e.toString());
    }
    return transactionCapsule;

  }

  public ShieldedAddressInfo getNewShieldedAddress() throws BadItemException, ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }

    ShieldedAddressInfo.Builder addressInfo = ShieldedAddressInfo.newBuilder();

    BytesMessage sk = getSpendingKey();
    DiversifierMessage d = getDiversifier();
    ExpandedSpendingKeyMessage expandedSpendingKeyMessage = getExpandedSpendingKey(sk.getValue());

    BytesMessage ak = getAkFromAsk(expandedSpendingKeyMessage.getAsk());
    BytesMessage nk = getNkFromNsk(expandedSpendingKeyMessage.getNsk());
    IncomingViewingKeyMessage ivk = getIncomingViewingKey(ak.getValue().toByteArray(),
        nk.getValue().toByteArray());

    PaymentAddressMessage addressMessage =
        getPaymentAddress(new IncomingViewingKey(ivk.getIvk().toByteArray()),
            new DiversifierT(d.getD().toByteArray()));

    addressInfo.setSk(sk.getValue());
    addressInfo.setAsk(expandedSpendingKeyMessage.getAsk());
    addressInfo.setNsk(expandedSpendingKeyMessage.getNsk());
    addressInfo.setOvk(expandedSpendingKeyMessage.getOvk());
    addressInfo.setAk(ak.getValue());
    addressInfo.setNk(nk.getValue());
    addressInfo.setIvk(ivk.getIvk());
    addressInfo.setD(d.getD());
    addressInfo.setPkD(addressMessage.getPkD());
    addressInfo.setPaymentAddress(addressMessage.getPaymentAddress());

    return addressInfo.build();

  }

  public BytesMessage getSpendingKey() throws ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    byte[] sk = SpendingKey.random().getValue();
    return BytesMessage.newBuilder().setValue(ByteString.copyFrom(sk)).build();
  }

  public ExpandedSpendingKeyMessage getExpandedSpendingKey(ByteString spendingKey)
      throws BadItemException, ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    if (Objects.isNull(spendingKey)) {
      throw new BadItemException("spendingKey is null");
    }
    if (ByteArray.toHexString(spendingKey.toByteArray()).length() != 64) {
      throw new BadItemException("the length of spendingKey's hex string should be 64");
    }

    ExpandedSpendingKey expandedSpendingKey = null;
    SpendingKey sk = new SpendingKey(spendingKey.toByteArray());
    expandedSpendingKey = sk.expandedSpendingKey();

    ExpandedSpendingKeyMessage.Builder responseBuild = ExpandedSpendingKeyMessage
        .newBuilder();
    responseBuild.setAsk(ByteString.copyFrom(expandedSpendingKey.getAsk()))
        .setNsk(ByteString.copyFrom(expandedSpendingKey.getNsk()))
        .setOvk(ByteString.copyFrom(expandedSpendingKey.getOvk()));

    return responseBuild.build();

  }

  public BytesMessage getAkFromAsk(ByteString ask) throws
      BadItemException, ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    if (Objects.isNull(ask)) {
      throw new BadItemException("ask is null");
    }
    if (ByteArray.toHexString(ask.toByteArray()).length() != 64) {
      throw new BadItemException("the length of ask's hex string should be 64");
    }

    byte[] ak = ExpandedSpendingKey.getAkFromAsk(ask.toByteArray());
    return BytesMessage.newBuilder().setValue(ByteString.copyFrom(ak)).build();
  }

  public BytesMessage getNkFromNsk(ByteString nsk) throws
      BadItemException, ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    if (Objects.isNull(nsk)) {
      throw new BadItemException("nsk is null");
    }
    if (ByteArray.toHexString(nsk.toByteArray()).length() != 64) {
      throw new BadItemException("the length of nsk's hex string should be 64");
    }

    byte[] nk = ExpandedSpendingKey.getNkFromNsk(nsk.toByteArray());
    return BytesMessage.newBuilder().setValue(ByteString.copyFrom(nk)).build();
  }

  public IncomingViewingKeyMessage getIncomingViewingKey(byte[] ak, byte[] nk)
      throws ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    byte[] ivk = new byte[32]; // the incoming viewing key
    JLibrustzcash.librustzcashCrhIvk(new CrhIvkParams(ak, nk, ivk));
    return IncomingViewingKeyMessage.newBuilder()
        .setIvk(ByteString.copyFrom(ivk))
        .build();
  }

  public DiversifierMessage getDiversifier() throws ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    byte[] d;
    while (true) {
      d = org.tron.keystore.Wallet.generateRandomBytes(Constant.ZC_DIVERSIFIER_SIZE);
      if (JLibrustzcash.librustzcashCheckDiversifier(d)) {
        break;
      }
    }
    DiversifierMessage diversifierMessage = DiversifierMessage.newBuilder()
        .setD(ByteString.copyFrom(d))
        .build();

    return diversifierMessage;
  }

  public BytesMessage getRcm() throws ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    byte[] rcm = Note.generateR();
    return BytesMessage.newBuilder().setValue(ByteString.copyFrom(rcm)).build();
  }

  public PaymentAddressMessage getPaymentAddress(IncomingViewingKey ivk,
      DiversifierT d) throws BadItemException, ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    if (!JLibrustzcash.librustzcashCheckDiversifier(d.getData())) {
      throw new BadItemException("d is not valid");
    }

    PaymentAddressMessage spa = null;
    Optional<PaymentAddress> op = ivk.address(d);
    if (op.isPresent()) {
      DiversifierMessage ds = DiversifierMessage.newBuilder()
          .setD(ByteString.copyFrom(d.getData()))
          .build();
      PaymentAddress paymentAddress = op.get();
      spa = PaymentAddressMessage.newBuilder()
          .setD(ds)
          .setPkD(ByteString.copyFrom(paymentAddress.getPkD()))
          .setPaymentAddress(KeyIo.encodePaymentAddress(paymentAddress))
          .build();
    }
    return spa;
  }

  public SpendResult isSpend(NoteParameters noteParameters) throws
      ZksnarkException, InvalidProtocolBufferException, BadItemException, ItemNotFoundException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    GrpcAPI.Note note = noteParameters.getNote();
    byte[] ak = noteParameters.getAk().toByteArray();
    byte[] nk = noteParameters.getNk().toByteArray();
    PaymentAddress paymentAddress = KeyIo.decodePaymentAddress(note.getPaymentAddress());
    if (paymentAddress == null) {
      throw new ZksnarkException(PAYMENT_ADDRESS_FORMAT_WRONG);
    }

    //only one OutPoint
    OutputPoint outputPoint = OutputPoint.newBuilder()
        .setHash(noteParameters.getTxid())
        .setIndex(noteParameters.getIndex())
        .build();
    OutputPointInfo outputPointInfo = OutputPointInfo.newBuilder()
        .addOutPoints(outputPoint)
        .setBlockNum(1) //constants
        .build();
    //most one voucher
    IncrementalMerkleVoucherInfo incrementalMerkleVoucherInfo =
        getMerkleTreeVoucherInfo(outputPointInfo);

    SpendResult result;
    if (incrementalMerkleVoucherInfo.getVouchersCount() == 0) {
      result = SpendResult.newBuilder()
          .setResult(false)
          .setMessage("The input note does not exist")
          .build();
      return result;
    }

    IncrementalMerkleVoucherContainer voucherContainer = new IncrementalMerkleVoucherCapsule(
        incrementalMerkleVoucherInfo.getVouchers(0)).toMerkleVoucherContainer();

    Note baseNote = new Note(paymentAddress.getD(), paymentAddress.getPkD(), note.getValue(),
        note.getRcm().toByteArray());
    byte[] nf = baseNote.nullifier(ak, nk, voucherContainer.position());

    if (dbManager.getNullfierStore().has(nf)) {
      result = SpendResult.newBuilder()
          .setResult(true)
          .setMessage("Input note has been spent")
          .build();
    } else {
      result = SpendResult.newBuilder()
          .setResult(false)
          .setMessage("The input note is not spent or does not exist")
          .build();
    }

    return result;
  }

  public BytesMessage createSpendAuthSig(SpendAuthSigParameters spendAuthSigParameters)
      throws ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    byte[] result = new byte[64];
    SpendSigParams spendSigPasrams = new SpendSigParams(
        spendAuthSigParameters.getAsk().toByteArray(),
        spendAuthSigParameters.getAlpha().toByteArray(),
        spendAuthSigParameters.getTxHash().toByteArray(),
        result);
    JLibrustzcash.librustzcashSaplingSpendSig(spendSigPasrams);

    return BytesMessage.newBuilder().setValue(ByteString.copyFrom(result)).build();
  }

  public BytesMessage createShieldNullifier(NfParameters nfParameters) throws ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    byte[] ak = nfParameters.getAk().toByteArray();
    byte[] nk = nfParameters.getNk().toByteArray();

    byte[] result = new byte[32]; // 256
    GrpcAPI.Note note = nfParameters.getNote();
    IncrementalMerkleVoucherCapsule incrementalMerkleVoucherCapsule
        = new IncrementalMerkleVoucherCapsule(nfParameters.getVoucher());
    IncrementalMerkleVoucherContainer incrementalMerkleVoucherContainer
        = new IncrementalMerkleVoucherContainer(incrementalMerkleVoucherCapsule);
    PaymentAddress paymentAddress = KeyIo.decodePaymentAddress(
        note.getPaymentAddress());
    if (paymentAddress == null) {
      throw new ZksnarkException(PAYMENT_ADDRESS_FORMAT_WRONG);
    }
    ComputeNfParams computeNfParams = new ComputeNfParams(
        paymentAddress.getD().getData(),
        paymentAddress.getPkD(),
        note.getValue(),
        note.getRcm().toByteArray(),
        ak,
        nk,
        incrementalMerkleVoucherContainer.position(),
        result);
    if (!JLibrustzcash.librustzcashComputeNf(computeNfParams)) {
      return null;
    }

    return BytesMessage.newBuilder()
        .setValue(ByteString.copyFrom(result))
        .build();
  }

  public BytesMessage getShieldTransactionHash(Transaction transaction)
      throws ContractValidateException, ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    List<Contract> contract = transaction.getRawData().getContractList();
    if (contract == null || contract.size() == 0) {
      throw new ContractValidateException("Contract is null");
    }
    ContractType contractType = contract.get(0).getType();
    if (contractType != ContractType.ShieldedTransferContract) {
      throw new ContractValidateException("Not a shielded transaction");
    }
    TransactionCapsule transactionCapsule = new TransactionCapsule(transaction);
    byte[] transactionHash = TransactionUtil
        .getShieldTransactionHashIgnoreTypeException(transactionCapsule.getInstance());
    return BytesMessage.newBuilder().setValue(ByteString.copyFrom(transactionHash)).build();
  }

  public TransactionInfoList getTransactionInfoByBlockNum(long blockNum) {
    TransactionInfoList.Builder transactionInfoList = TransactionInfoList.newBuilder();

    try {
      TransactionRetCapsule result = dbManager.getTransactionRetStore()
          .getTransactionInfoByBlockNum(ByteArray.fromLong(blockNum));

      if (!Objects.isNull(result) && !Objects.isNull(result.getInstance())) {
        result.getInstance().getTransactioninfoList().forEach(
            transactionInfo -> transactionInfoList.addTransactionInfo(transactionInfo)
        );
      } else {
        Block block = dbManager.getBlockByNum(blockNum).getInstance();

        if (block != null) {
          List<Transaction> listTransaction = block.getTransactionsList();
          for (Transaction transaction : listTransaction) {
            TransactionInfoCapsule transactionInfoCapsule = dbManager.getTransactionHistoryStore()
                .get(Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(),
                    transaction.getRawData().toByteArray()));
            transactionInfoList.addTransactionInfo(transactionInfoCapsule.getInstance());
          }
        }
      }
    } catch (BadItemException | ItemNotFoundException e) {
      logger.error(e.getMessage());
    }

    return transactionInfoList.build();
  }

  public NodeList listNodes() {
    List<NodeHandler> handlerList = nodeManager.dumpActiveNodes();

    Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();
    for (NodeHandler handler : handlerList) {
      String key = handler.getNode().getHexId() + handler.getNode().getHost();
      nodeHandlerMap.put(key, handler);
    }

    NodeList.Builder nodeListBuilder = NodeList.newBuilder();

    nodeHandlerMap.entrySet().stream()
        .forEach(v -> {
          org.tron.common.overlay.discover.node.Node node = v.getValue()
              .getNode();
          nodeListBuilder.addNodes(Node.newBuilder().setAddress(
              Address.newBuilder()
                  .setHost(ByteString
                      .copyFrom(ByteArray.fromString(node.getHost())))
                  .setPort(node.getPort())));
        });
    return nodeListBuilder.build();
  }

  public Transaction deployContract(CreateSmartContract createSmartContract,
      TransactionCapsule trxCap) {

    // do nothing, so can add some useful function later
    // trxcap contract para cacheUnpackValue has value
    return trxCap.getInstance();
  }

  public Transaction triggerContract(TriggerSmartContract
      triggerSmartContract,
      TransactionCapsule trxCap, Builder builder,
      Return.Builder retBuilder)
      throws ContractValidateException, ContractExeException, HeaderNotFound, VMIllegalException {

    ContractStore contractStore = dbManager.getContractStore();
    byte[] contractAddress = triggerSmartContract.getContractAddress()
        .toByteArray();
    SmartContract.ABI abi = contractStore.getABI(contractAddress);
    if (abi == null) {
      throw new ContractValidateException(
          "No contract or not a valid smart contract");
    }

    byte[] selector = getSelector(
        triggerSmartContract.getData().toByteArray());

    if (WalletUtil.isConstant(abi, selector)) {
      return callConstantContract(trxCap, builder, retBuilder);
    } else {
      return trxCap.getInstance();
    }
  }

  public Transaction triggerConstantContract(TriggerSmartContract
      triggerSmartContract,
      TransactionCapsule trxCap, Builder builder,
      Return.Builder retBuilder)
      throws ContractValidateException, ContractExeException, HeaderNotFound, VMIllegalException {

    ContractStore contractStore = dbManager.getContractStore();
    byte[] contractAddress = triggerSmartContract.getContractAddress()
        .toByteArray();
    byte[] isContractExiste = contractStore
        .findContractByHash(contractAddress);

    if (ArrayUtils.isEmpty(isContractExiste)) {
      throw new ContractValidateException(
          "No contract or not a smart contract");
    }

    if (!Args.getInstance().isSupportConstant()) {
      throw new ContractValidateException("this node does not support constant");
    }

    return callConstantContract(trxCap, builder, retBuilder);
  }

  public Transaction callConstantContract(TransactionCapsule trxCap, Builder
      builder,
      Return.Builder retBuilder)
      throws ContractValidateException, ContractExeException, HeaderNotFound, VMIllegalException {

    if (!Args.getInstance().isSupportConstant()) {
      throw new ContractValidateException("this node does not support constant");
    }
    DepositImpl deposit = DepositImpl.createRoot(dbManager);

    Block headBlock;
    List<BlockCapsule> blockCapsuleList = dbManager.getBlockStore()
        .getBlockByLatestNum(1);
    if (CollectionUtils.isEmpty(blockCapsuleList)) {
      throw new HeaderNotFound("latest block not found");
    } else {
      headBlock = blockCapsuleList.get(0).getInstance();
    }

    TransactionContext context = new TransactionContext(new BlockCapsule(headBlock), trxCap,
        StoreFactory.getInstance(), true,
        false);
    /*VMConfig.initVmHardFork(ForkController.instance().pass(ForkBlockVersionConsts.ENERGY_LIMIT));
    VMConfig.initAllowMultiSign(dbManager.getDynamicPropertiesStore().getAllowMultiSign());
    VMConfig.initAllowTvmTransferTrc10(
        dbManager.getDynamicPropertiesStore().getAllowTvmTransferTrc10());
    VMConfig.initAllowTvmConstantinople(
        dbManager.getDynamicPropertiesStore().getAllowTvmConstantinople());
    VMConfig
        .initAllowTvmSolidity059(dbManager.getDynamicPropertiesStore().getAllowTvmSolidity059());*/
    VMActuator vmActuator = new VMActuator(true);

    vmActuator.validate(context);
    // VMConfig.handleProposalInVM(vmActuator.getRepository());
    vmActuator.execute(context);

    ProgramResult result = context.getProgramResult();
    if (result.getException() != null) {
      RuntimeException e = result.getException();
      logger.warn("Constant call has error {}", e.getMessage());
      throw e;
    }

    TransactionResultCapsule ret = new TransactionResultCapsule();

    builder.addConstantResult(ByteString.copyFrom(result.getHReturn()));
    ret.setStatus(0, code.SUCESS);
    if (StringUtils.isNoneEmpty(result.getRuntimeError())) {
      ret.setStatus(0, code.FAILED);
      retBuilder
          .setMessage(ByteString.copyFromUtf8(result.getRuntimeError()))
          .build();
    }
    if (result.isRevert()) {
      ret.setStatus(0, code.FAILED);
      retBuilder.setMessage(ByteString.copyFromUtf8("REVERT opcode executed"))
          .build();
    }
    trxCap.setResult(ret);
    return trxCap.getInstance();
  }

  public SmartContract getContract(GrpcAPI.BytesMessage bytesMessage) {
    byte[] address = bytesMessage.getValue().toByteArray();
    AccountCapsule accountCapsule = dbManager.getAccountStore().get(address);
    if (accountCapsule == null) {
      logger.error(
          "Get contract failed, the account does not exist or the account does not have a code hash!");
      return null;
    }

    ContractCapsule contractCapsule = dbManager.getContractStore()
        .get(bytesMessage.getValue().toByteArray());
    if (Objects.nonNull(contractCapsule)) {
      return contractCapsule.getInstance();
    }
    return null;
  }

  /*
  input
  offset:100,limit:10
  return
  id: 101~110
   */
  public SideChainProposalList getPaginatedProposalList(long offset, long limit) {

    if (limit < 0 || offset < 0) {
      return null;
    }

    long latestProposalNum = dbManager.getDynamicPropertiesStore()
        .getLatestProposalNum();
    if (latestProposalNum <= offset) {
      return null;
    }
    limit =
        limit > PROPOSAL_COUNT_LIMIT_MAX ? PROPOSAL_COUNT_LIMIT_MAX : limit;
    long end = offset + limit;
    end = end > latestProposalNum ? latestProposalNum : end;
    SideChainProposalList.Builder builder = SideChainProposalList.newBuilder();

    ImmutableList<Long> rangeList = ContiguousSet
        .create(Range.openClosed(offset, end), DiscreteDomain.longs())
        .asList();
    rangeList.stream().map(ProposalCapsule::calculateDbKey).map(key -> {
      try {
        return dbManager.getProposalStore().get(key);
      } catch (Exception ex) {
        return null;
      }
    }).filter(Objects::nonNull)
        .forEach(proposalCapsule -> builder
            .addProposals(proposalCapsule.getInstance()));
    return builder.build();
  }

  public ExchangeList getPaginatedExchangeList(long offset, long limit) {
    if (limit < 0 || offset < 0) {
      return null;
    }

    long latestExchangeNum = dbManager.getDynamicPropertiesStore()
        .getLatestExchangeNum();
    if (latestExchangeNum <= offset) {
      return null;
    }
    limit =
        limit > EXCHANGE_COUNT_LIMIT_MAX ? EXCHANGE_COUNT_LIMIT_MAX : limit;
    long end = offset + limit;
    end = end > latestExchangeNum ? latestExchangeNum : end;

    ExchangeList.Builder builder = ExchangeList.newBuilder();
    ImmutableList<Long> rangeList = ContiguousSet
        .create(Range.openClosed(offset, end), DiscreteDomain.longs())
        .asList();
    rangeList.stream().map(ExchangeCapsule::calculateDbKey).map(key -> {
      try {
        return dbManager.getExchangeStoreFinal().get(key);
      } catch (Exception ex) {
        return null;
      }
    }).filter(Objects::nonNull)
        .forEach(exchangeCapsule -> builder
            .addExchanges(exchangeCapsule.getInstance()));
    return builder.build();

  }

  /*
   * strip right 0 from memo
   */
  public byte[] stripRightZero(byte[] memo) {
    int index = memo.length;
    for (; index > 0; --index) {
      if (memo[index - 1] != 0) {
        break;
      }
    }
    byte[] memoStrip = new byte[index];
    System.arraycopy(memo, 0, memoStrip, 0, index);
    return memoStrip;
  }

  /*
   * query note by ivk
   */
  private GrpcAPI.DecryptNotes queryNoteByIvk(long startNum, long endNum, byte[] ivk)
      throws BadItemException, ZksnarkException {
    GrpcAPI.DecryptNotes.Builder builder = GrpcAPI.DecryptNotes.newBuilder();
    if (!(startNum >= 0 && endNum > startNum && endNum - startNum <= 1000)) {
      throw new BadItemException(
          "request requires start_block_index >= 0 && end_block_index > start_block_index "
              + "&& end_block_index - start_block_index <= 1000");
    }
    BlockList blockList = this.getBlocksByLimitNext(startNum, endNum - startNum);
    for (Block block : blockList.getBlockList()) {
      for (Transaction transaction : block.getTransactionsList()) {
        TransactionCapsule transactionCapsule = new TransactionCapsule(transaction);
        byte[] txid = transactionCapsule.getTransactionId().getBytes();
        List<Transaction.Contract> contracts = transaction.getRawData().getContractList();
        if (contracts.isEmpty()) {
          continue;
        }
        Transaction.Contract c = contracts.get(0);
        if (c.getType() != Contract.ContractType.ShieldedTransferContract) {
          continue;
        }
        ShieldedTransferContract stContract;
        try {
          stContract = c.getParameter().unpack(ShieldedTransferContract.class);
        } catch (InvalidProtocolBufferException e) {
          throw new ZksnarkException(
              "unpack ShieldedTransferContract failed.");
        }

        for (int index = 0; index < stContract.getReceiveDescriptionList().size(); index++) {
          ReceiveDescription r = stContract.getReceiveDescription(index);
          Optional<Note> notePlaintext = Note.decrypt(r.getCEnc().toByteArray(),//ciphertext
              ivk,
              r.getEpk().toByteArray(),//epk
              r.getNoteCommitment().toByteArray() //cmu
          );

          if (notePlaintext.isPresent()) {
            Note noteText = notePlaintext.get();
            byte[] pkD = new byte[32];
            if (!JLibrustzcash
                .librustzcashIvkToPkd(new IvkToPkdParams(ivk, noteText.getD().getData(),
                    pkD))) {
              continue;
            }

            String paymentAddress = KeyIo
                .encodePaymentAddress(new PaymentAddress(noteText.getD(), pkD));
            GrpcAPI.Note note = GrpcAPI.Note.newBuilder()
                .setPaymentAddress(paymentAddress)
                .setValue(noteText.getValue())
                .setRcm(ByteString.copyFrom(noteText.getRcm()))
                .setMemo(ByteString.copyFrom(stripRightZero(noteText.getMemo())))
                .build();
            DecryptNotes.NoteTx noteTx = DecryptNotes.NoteTx.newBuilder().setNote(note)
                .setTxid(ByteString.copyFrom(txid)).setIndex(index).build();

            builder.addNoteTxs(noteTx);
          }
        } // end of ReceiveDescriptionList
      } // end of transaction
    } //end of blocklist
    return builder.build();
  }

  /*
   * try to get all note belongs to ivk
   */
  public GrpcAPI.DecryptNotes scanNoteByIvk(long startNum, long endNum,
      byte[] ivk) throws BadItemException, ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    GrpcAPI.DecryptNotes notes = queryNoteByIvk(startNum, endNum, ivk);
    return notes;
  }

  /*
  try to get unspent note belongs to ivk
   */
  public GrpcAPI.DecryptNotesMarked scanAndMarkNoteByIvk(long startNum, long endNum,
      byte[] ivk, byte[] ak, byte[] nk) throws BadItemException, ZksnarkException,
      InvalidProtocolBufferException, ItemNotFoundException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }

    GrpcAPI.DecryptNotes srcNotes = queryNoteByIvk(startNum, endNum, ivk);
    GrpcAPI.DecryptNotesMarked.Builder builder = GrpcAPI.DecryptNotesMarked.newBuilder();
    for (NoteTx noteTx : srcNotes.getNoteTxsList()) {
      //query if note is already spent
      NoteParameters noteParameters = NoteParameters.newBuilder()
          .setNote(noteTx.getNote())
          .setAk(ByteString.copyFrom(ak))
          .setNk(ByteString.copyFrom(nk))
          .setTxid(noteTx.getTxid())
          .setIndex(noteTx.getIndex())
          .build();
      SpendResult spendResult = isSpend(noteParameters);

      //construct DecryptNotesMarked
      GrpcAPI.DecryptNotesMarked.NoteTx.Builder markedNoteTx
          = GrpcAPI.DecryptNotesMarked.NoteTx.newBuilder();
      markedNoteTx.setNote(noteTx.getNote());
      markedNoteTx.setTxid(noteTx.getTxid());
      markedNoteTx.setIndex(noteTx.getIndex());
      markedNoteTx.setIsSpend(spendResult.getResult());

      builder.addNoteTxs(markedNoteTx);
    }
    return builder.build();
  }

  /*
   * try to get cm belongs to ovk
   */
  public GrpcAPI.DecryptNotes scanNoteByOvk(long startNum, long endNum,
      byte[] ovk) throws BadItemException, ZksnarkException {
    if (!getFullNodeAllowShieldedTransaction()) {
      throw new ZksnarkException(SHIELDED_ID_NOT_ALLOWED);
    }
    GrpcAPI.DecryptNotes.Builder builder = GrpcAPI.DecryptNotes.newBuilder();
    if (!(startNum >= 0 && endNum > startNum && endNum - startNum <= 1000)) {
      throw new BadItemException(
          "request require start_block_index >= 0 && end_block_index > start_block_index "
              + "&& end_block_index - start_block_index <= 1000");
    }
    BlockList blockList = this.getBlocksByLimitNext(startNum, endNum - startNum);
    for (Block block : blockList.getBlockList()) {
      for (Transaction transaction : block.getTransactionsList()) {
        TransactionCapsule transactionCapsule = new TransactionCapsule(transaction);
        byte[] txid = transactionCapsule.getTransactionId().getBytes();
        List<Transaction.Contract> contracts = transaction.getRawData().getContractList();
        if (contracts.size() == 0) {
          continue;
        }
        Transaction.Contract c = contracts.get(0);
        if (c.getType() != Protocol.Transaction.Contract.ContractType.ShieldedTransferContract) {
          continue;
        }
        ShieldedTransferContract stContract = null;
        try {
          stContract = c.getParameter().unpack(
              ShieldedTransferContract.class);
        } catch (InvalidProtocolBufferException e) {
          throw new RuntimeException(
              "unpack ShieldedTransferContract failed.");
        }

        for (int index = 0; index < stContract.getReceiveDescriptionList().size(); index++) {
          ReceiveDescription r = stContract.getReceiveDescription(index);
          Encryption.OutCiphertext cOut = new Encryption.OutCiphertext();
          cOut.setData(r.getCOut().toByteArray());
          Optional<OutgoingPlaintext> notePlaintext = OutgoingPlaintext.decrypt(cOut,//ciphertext
              ovk,
              r.getValueCommitment().toByteArray(), //cv
              r.getNoteCommitment().toByteArray(), //cmu
              r.getEpk().toByteArray() //epk
          );

          if (notePlaintext.isPresent()) {
            OutgoingPlaintext decryptedOutCtUnwrapped = notePlaintext.get();
            //decode c_enc with pkd、esk
            Encryption.EncCiphertext ciphertext = new Encryption.EncCiphertext();
            ciphertext.setData(r.getCEnc().toByteArray());
            Optional<Note> foo = Note.decrypt(ciphertext,
                r.getEpk().toByteArray(),
                decryptedOutCtUnwrapped.getEsk(),
                decryptedOutCtUnwrapped.getPkD(),
                r.getNoteCommitment().toByteArray());

            if (foo.isPresent()) {
              Note bar = foo.get();
              String paymentAddress = KeyIo.encodePaymentAddress(
                  new PaymentAddress(bar.getD(), decryptedOutCtUnwrapped.getPkD()));
              GrpcAPI.Note note = GrpcAPI.Note.newBuilder()
                  .setPaymentAddress(paymentAddress)
                  .setValue(bar.getValue())
                  .setRcm(ByteString.copyFrom(bar.getRcm()))
                  .setMemo(ByteString.copyFrom(stripRightZero(bar.getMemo())))
                  .build();

              DecryptNotes.NoteTx noteTx = DecryptNotes.NoteTx
                  .newBuilder()
                  .setNote(note)
                  .setTxid(ByteString.copyFrom(txid))
                  .setIndex(index)
                  .build();

              builder.addNoteTxs(noteTx);
            }
          }
        } // end of ReceiveDescriptionList
      } // end of transaction
    } //end of blocklist
    return builder.build();
  }

  public String convertGateWayListToString(List<byte[]> list) {
    String gateWayStr = "";
    int i = 0;
    while (i < list.size() - 1) {
      gateWayStr += WalletUtil.encode58Check(list.get(i)) + ",";
      i++;
    }
    if (i == list.size() - 1) {
      gateWayStr += WalletUtil.encode58Check(list.get(i));
    }
    return gateWayStr;
  }
}

