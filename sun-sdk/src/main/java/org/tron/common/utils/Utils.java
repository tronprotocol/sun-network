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

package org.tron.common.utils;

import com.google.protobuf.ByteString;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.common.crypto.Sha256Hash;
import org.tron.protos.Contract.AccountCreateContract;
import org.tron.protos.Contract.AccountPermissionUpdateContract;
import org.tron.protos.Contract.AccountUpdateContract;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Contract.AssetIssueContract.FrozenSupply;
import org.tron.protos.Contract.CreateSmartContract;
import org.tron.protos.Contract.ExchangeCreateContract;
import org.tron.protos.Contract.ExchangeInjectContract;
import org.tron.protos.Contract.ExchangeTransactionContract;
import org.tron.protos.Contract.ExchangeWithdrawContract;
import org.tron.protos.Contract.FreezeBalanceContract;
import org.tron.protos.Contract.ParticipateAssetIssueContract;
import org.tron.protos.Contract.ProposalApproveContract;
import org.tron.protos.Contract.ProposalCreateContract;
import org.tron.protos.Contract.ProposalDeleteContract;
import org.tron.protos.Contract.SideChainProposalCreateContract;
import org.tron.protos.Contract.TransferAssetContract;
import org.tron.protos.Contract.TransferContract;
import org.tron.protos.Contract.TriggerSmartContract;
import org.tron.protos.Contract.UnfreezeAssetContract;
import org.tron.protos.Contract.UnfreezeBalanceContract;
import org.tron.protos.Contract.UpdateAssetContract;
import org.tron.protos.Contract.UpdateEnergyLimitContract;
import org.tron.protos.Contract.UpdateSettingContract;
import org.tron.protos.Contract.VoteAssetContract;
import org.tron.protos.Contract.VoteWitnessContract;
import org.tron.protos.Contract.WithdrawBalanceContract;
import org.tron.protos.Contract.WitnessCreateContract;
import org.tron.protos.Contract.WitnessUpdateContract;
import org.tron.protos.Protocol.Key;
import org.tron.protos.Protocol.Permission;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract;
import org.tron.protos.Protocol.Transaction.Result;

public class Utils {

  private static SecureRandom random = new SecureRandom();

  public static SecureRandom getRandom() {
    return random;
  }

  public static byte[] getBytes(char[] chars) {
    Charset cs = Charset.forName("UTF-8");
    CharBuffer cb = CharBuffer.allocate(chars.length);
    cb.put(chars);
    cb.flip();
    ByteBuffer bb = cs.encode(cb);

    return bb.array();
  }

  /**
   * yyyy-MM-dd
   */
  public static Date strToDateLong(String strDate) {
    if (strDate.length() == 10) {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      ParsePosition pos = new ParsePosition(0);
      Date strtodate = formatter.parse(strDate, pos);
      return strtodate;
    } else if (strDate.length() == 19) {
      strDate = strDate.replace("_", " ");
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      ParsePosition pos = new ParsePosition(0);
      Date strtodate = formatter.parse(strDate, pos);
      return strtodate;
    }
    return null;
  }

  public static String printAssetIssue(AssetIssueContract assetIssue) {
    String result = "";
    result += "id: ";
    result += assetIssue.getId();
    result += "\n";
    result += "owner_address: ";
    result += AddressUtil.encode58Check(assetIssue.getOwnerAddress().toByteArray());
    result += "\n";
    result += "name: ";
    result += new String(assetIssue.getName().toByteArray(), Charset.forName("UTF-8"));
    result += "\n";
    result += "order: ";
    result += assetIssue.getOrder();
    result += "\n";
    result += "total_supply: ";
    result += assetIssue.getTotalSupply();
    result += "\n";
    result += "trx_num: ";
    result += assetIssue.getTrxNum();
    result += "\n";
    result += "num: ";
    result += assetIssue.getNum();
    result += "\n";
    result += "precision ";
    result += assetIssue.getPrecision();
    result += "\n";
    result += "start_time: ";
    result += new Date(assetIssue.getStartTime());
    result += "\n";
    result += "end_time: ";
    result += new Date(assetIssue.getEndTime());
    result += "\n";
    result += "vote_score: ";
    result += assetIssue.getVoteScore();
    result += "\n";
    result += "description: ";
    result += new String(assetIssue.getDescription().toByteArray(), Charset.forName("UTF-8"));
    result += "\n";
    result += "url: ";
    result += new String(assetIssue.getUrl().toByteArray(), Charset.forName("UTF-8"));
    result += "\n";
    result += "free asset net limit: ";
    result += assetIssue.getFreeAssetNetLimit();
    result += "\n";
    result += "public free asset net limit: ";
    result += assetIssue.getPublicFreeAssetNetLimit();
    result += "\n";
    result += "public free asset net usage: ";
    result += assetIssue.getPublicFreeAssetNetUsage();
    result += "\n";
    result += "public latest free net time: ";
    result += assetIssue.getPublicLatestFreeNetTime();
    result += "\n";

    if (assetIssue.getFrozenSupplyCount() > 0) {
      for (FrozenSupply frozenSupply : assetIssue.getFrozenSupplyList()) {
        result += "frozen_supply";
        result += "\n";
        result += "{";
        result += "\n";
        result += "  amount: ";
        result += frozenSupply.getFrozenAmount();
        result += "\n";
        result += "  frozen_days: ";
        result += frozenSupply.getFrozenDays();
        result += "\n";
        result += "}";
        result += "\n";
      }
    }

    if (assetIssue.getId().equals("")) {
      result += "\n";
      result += "Note: In 3.2, you can use getAssetIssueById or getAssetIssueListByName, because 3.2 allows same token name.";
      result += "\n";
    }
    return result;
  }

  public static String printContract(Transaction.Contract contract) {
    String result = "";
    try {
      result += "contract_type: ";
      result += contract.getType();
      result += "\n";

      switch (contract.getType()) {
        case AccountCreateContract:
          AccountCreateContract accountCreateContract = contract.getParameter()
              .unpack(AccountCreateContract.class);
          result += "type: ";
          result += accountCreateContract.getType();
          result += "\n";
          if (accountCreateContract.getAccountAddress() != null
              && !accountCreateContract.getAccountAddress().isEmpty()) {
            result += "account_address: ";
            result += AddressUtil
                .encode58Check(accountCreateContract.getAccountAddress().toByteArray());
            result += "\n";
          }
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(accountCreateContract.getOwnerAddress().toByteArray());
          result += "\n";
          break;
        case AccountUpdateContract:
          AccountUpdateContract accountUpdateContract = contract.getParameter()
              .unpack(AccountUpdateContract.class);
          if (accountUpdateContract.getAccountName() != null
              && !accountUpdateContract.getAccountName().isEmpty()) {
            result += "account_name: ";
            result += new String(accountUpdateContract.getAccountName().toByteArray(),
                Charset.forName("UTF-8"));
            result += "\n";
          }
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(accountUpdateContract.getOwnerAddress().toByteArray());
          result += "\n";
          break;
        case TransferContract:
          TransferContract transferContract = contract.getParameter()
              .unpack(TransferContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(transferContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "to_address: ";
          result += AddressUtil
              .encode58Check(transferContract.getToAddress().toByteArray());
          result += "\n";
          result += "amount: ";
          result += transferContract.getAmount();
          result += "\n";
          break;
        case TransferAssetContract:
          TransferAssetContract transferAssetContract = contract.getParameter()
              .unpack(TransferAssetContract.class);
          result += "asset_name: ";
          result += new String(transferAssetContract.getAssetName().toByteArray(),
              Charset.forName("UTF-8"));
          result += "\n";
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(transferAssetContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "to_address: ";
          result += AddressUtil
              .encode58Check(transferAssetContract.getToAddress().toByteArray());
          result += "\n";
          result += "amount: ";
          result += transferAssetContract.getAmount();
          result += "\n";
          break;
        case VoteAssetContract:
          VoteAssetContract voteAssetContract = contract.getParameter()
              .unpack(VoteAssetContract.class);
          break;
        case VoteWitnessContract:
          VoteWitnessContract voteWitnessContract = contract.getParameter()
              .unpack(VoteWitnessContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(voteWitnessContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "votes: ";
          result += "\n";
          result += "{";
          result += "\n";
          for (VoteWitnessContract.Vote vote : voteWitnessContract.getVotesList()) {
            result += "[";
            result += "\n";
            result += "vote_address: ";
            result += AddressUtil
                .encode58Check(vote.getVoteAddress().toByteArray());
            result += "\n";
            result += "vote_count: ";
            result += vote.getVoteCount();
            result += "\n";
            result += "]";
            result += "\n";
          }
          result += "}";
          result += "\n";
          break;
        case WitnessCreateContract:
          WitnessCreateContract witnessCreateContract = contract.getParameter()
              .unpack(WitnessCreateContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(witnessCreateContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "url: ";
          result += new String(witnessCreateContract.getUrl().toByteArray(),
              Charset.forName("UTF-8"));
          result += "\n";
          break;
        case WitnessUpdateContract:
          WitnessUpdateContract witnessUpdateContract = contract.getParameter()
              .unpack(WitnessUpdateContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(witnessUpdateContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "url: ";
          result += new String(witnessUpdateContract.getUpdateUrl().toByteArray(),
              Charset.forName("UTF-8"));
          result += "\n";
          break;
        case AssetIssueContract:
          AssetIssueContract assetIssueContract = contract.getParameter()
              .unpack(AssetIssueContract.class);
          result += printAssetIssue(assetIssueContract);
          break;
        case UpdateAssetContract:
          UpdateAssetContract updateAssetContract = contract.getParameter()
              .unpack(UpdateAssetContract.class);
          result += "owner_address: ";
          result += AddressUtil.encode58Check(updateAssetContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "description: ";
          result += new String(updateAssetContract.getDescription().toByteArray(),
              Charset.forName("UTF-8"));
          result += "\n";
          result += "url: ";
          result += new String(updateAssetContract.getUrl().toByteArray(),
              Charset.forName("UTF-8"));
          result += "\n";
          result += "free asset net limit: ";
          result += updateAssetContract.getNewLimit();
          result += "\n";
          result += "public free asset net limit: ";
          result += updateAssetContract.getNewPublicLimit();
          result += "\n";
          break;
        case ParticipateAssetIssueContract:
          ParticipateAssetIssueContract participateAssetIssueContract = contract.getParameter()
              .unpack(ParticipateAssetIssueContract.class);
          result += "asset_name: ";
          result += new String(participateAssetIssueContract.getAssetName().toByteArray(),
              Charset.forName("UTF-8"));
          result += "\n";
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(participateAssetIssueContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "to_address: ";
          result += AddressUtil
              .encode58Check(participateAssetIssueContract.getToAddress().toByteArray());
          result += "\n";
          result += "amount: ";
          result += participateAssetIssueContract.getAmount();
          result += "\n";
          break;
        case FreezeBalanceContract:
          FreezeBalanceContract freezeBalanceContract = contract.getParameter()
              .unpack(FreezeBalanceContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(freezeBalanceContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "receive_address: ";
          result += AddressUtil
              .encode58Check(freezeBalanceContract.getReceiverAddress().toByteArray());
          result += "\n";
          result += "frozen_balance: ";
          result += freezeBalanceContract.getFrozenBalance();
          result += "\n";
          result += "frozen_duration: ";
          result += freezeBalanceContract.getFrozenDuration();
          result += "\n";
          break;
        case UnfreezeBalanceContract:
          UnfreezeBalanceContract unfreezeBalanceContract = contract.getParameter()
              .unpack(UnfreezeBalanceContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(unfreezeBalanceContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "receive_address: ";
          result += AddressUtil
              .encode58Check(unfreezeBalanceContract.getReceiverAddress().toByteArray());
          result += "\n";
          break;
        case UnfreezeAssetContract:
          UnfreezeAssetContract unfreezeAssetContract = contract.getParameter()
              .unpack(UnfreezeAssetContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(unfreezeAssetContract.getOwnerAddress().toByteArray());
          result += "\n";
          break;
        case WithdrawBalanceContract:
          WithdrawBalanceContract withdrawBalanceContract = contract.getParameter()
              .unpack(WithdrawBalanceContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(withdrawBalanceContract.getOwnerAddress().toByteArray());
          result += "\n";
          break;
        case CreateSmartContract:
          CreateSmartContract createSmartContract = contract.getParameter()
              .unpack(CreateSmartContract.class);
          SmartContract newContract = createSmartContract.getNewContract();
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(createSmartContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "ABI: ";
          result += newContract.getAbi().toString();
          result += "\n";
          result += "byte_code: ";
          result += ByteArray.toHexString(newContract.getBytecode().toByteArray());
          result += "\n";
          result += "call_value: ";
          result += newContract.getCallValue();
          result += "\n";
          result += "contract_address:";
          result += AddressUtil.encode58Check(newContract.getContractAddress().toByteArray());
          result += "\n";
          break;
        case TriggerSmartContract:
          TriggerSmartContract triggerSmartContract = contract.getParameter()
              .unpack(TriggerSmartContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(triggerSmartContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "contract_address: ";
          result += AddressUtil
              .encode58Check(triggerSmartContract.getContractAddress().toByteArray());
          result += "\n";
          result += "call_value:";
          result += triggerSmartContract.getCallValue();
          result += "\n";
          result += "data:";
          result += ByteArray.toHexString(triggerSmartContract.getData().toByteArray());
          result += "\n";
          break;
        case ProposalCreateContract:
          ProposalCreateContract proposalCreateContract = contract.getParameter()
              .unpack(ProposalCreateContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(proposalCreateContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "parametersMap: ";
          result += proposalCreateContract.getParametersMap();
          result += "\n";
          break;
        case ProposalApproveContract:
          ProposalApproveContract proposalApproveContract = contract.getParameter()
              .unpack(ProposalApproveContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(proposalApproveContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "proposal id: ";
          result += proposalApproveContract.getProposalId();
          result += "\n";
          result += "IsAddApproval: ";
          result += proposalApproveContract.getIsAddApproval();
          result += "\n";
          break;
        case ProposalDeleteContract:
          ProposalDeleteContract proposalDeleteContract = contract.getParameter()
              .unpack(ProposalDeleteContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(proposalDeleteContract.getOwnerAddress().toByteArray());
          break;
        case ExchangeCreateContract:
          ExchangeCreateContract exchangeCreateContract = contract.getParameter()
              .unpack(ExchangeCreateContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(exchangeCreateContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "firstTokenId: ";
          result += exchangeCreateContract.getFirstTokenId().toStringUtf8();
          result += "\n";
          result += "firstTokenBalance: ";
          result += exchangeCreateContract.getFirstTokenBalance();
          result += "\n";
          result += "secondTokenId: ";
          result += exchangeCreateContract.getSecondTokenId().toStringUtf8();
          result += "\n";
          result += "secondTokenBalance: ";
          result += exchangeCreateContract.getSecondTokenBalance();
          result += "\n";
          break;
        case ExchangeInjectContract:
          ExchangeInjectContract exchangeInjectContract = contract.getParameter()
              .unpack(ExchangeInjectContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(exchangeInjectContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "TokenId: ";
          result += exchangeInjectContract.getTokenId().toStringUtf8();
          result += "\n";
          result += "quant: ";
          result += exchangeInjectContract.getQuant();
          result += "\n";
          break;
        case ExchangeWithdrawContract:
          ExchangeWithdrawContract exchangeWithdrawContract = contract.getParameter()
              .unpack(ExchangeWithdrawContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(exchangeWithdrawContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "TokenId: ";
          result += exchangeWithdrawContract.getTokenId().toStringUtf8();
          result += "\n";
          result += "quant: ";
          result += exchangeWithdrawContract.getQuant();
          result += "\n";
          break;
        case ExchangeTransactionContract:
          ExchangeTransactionContract exchangeTransactionContract = contract.getParameter()
              .unpack(ExchangeTransactionContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(exchangeTransactionContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "TokenId: ";
          result += exchangeTransactionContract.getTokenId().toStringUtf8();
          result += "\n";
          result += "quant: ";
          result += exchangeTransactionContract.getQuant();
          result += "\n";
          break;
        case AccountPermissionUpdateContract:
          AccountPermissionUpdateContract accountPermissionUpdateContract = contract.getParameter()
              .unpack(AccountPermissionUpdateContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(accountPermissionUpdateContract.getOwnerAddress().toByteArray());
          result += "\n";
          if (accountPermissionUpdateContract.hasOwner()) {
            result += "owner_permission: ";
            result += "\n";
            result += "{";
            result += "\n";
            result += printPermission(accountPermissionUpdateContract.getOwner());
            result += "\n";
            result += "}";
            result += "\n";
          }

          if (accountPermissionUpdateContract.hasWitness()) {
            result += "witness_permission: ";
            result += "\n";
            result += "{";
            result += "\n";
            result += printPermission(accountPermissionUpdateContract.getWitness());
            result += "\n";
            result += "}";
            result += "\n";
          }

          if (accountPermissionUpdateContract.getActivesCount() > 0) {
            result += "active_permissions: ";
            result += printPermissionList(accountPermissionUpdateContract.getActivesList());
            result += "\n";
          }
          break;
        case UpdateSettingContract:
          UpdateSettingContract updateSettingContract = contract.getParameter()
              .unpack(UpdateSettingContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(updateSettingContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "contract_address: ";
          result += AddressUtil
              .encode58Check(updateSettingContract.getContractAddress().toByteArray());
          result += "\n";
          result += "consume_user_resource_percent: ";
          result += updateSettingContract.getConsumeUserResourcePercent();
          result += "\n";
          break;
        case UpdateEnergyLimitContract:
          UpdateEnergyLimitContract updateEnergyLimitContract = contract.getParameter()
              .unpack(UpdateEnergyLimitContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(updateEnergyLimitContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "contract_address: ";
          result += AddressUtil
              .encode58Check(updateEnergyLimitContract.getContractAddress().toByteArray());
          result += "\n";
          result += "origin_energy_limit: ";
          result += updateEnergyLimitContract.getOriginEnergyLimit();
          result += "\n";
          break;
        // case BuyStorageContract:
        //   BuyStorageContract buyStorageContract = contract.getParameter()
        //       .unpack(BuyStorageContract.class);
        //   result += "owner_address: ";
        //   result += ServerApi
        //       .encode58Check(buyStorageContract.getOwnerAddress().toByteArray());
        //   result += "\n";
        //   result += "quant:";
        //   result += buyStorageContract.getQuant();
        //   result += "\n";
        //   break;
        // case SellStorageContract:
        //   SellStorageContract sellStorageContract = contract.getParameter()
        //       .unpack(SellStorageContract.class);
        //   result += "owner_address: ";
        //   result += ServerApi
        //       .encode58Check(sellStorageContract.getOwnerAddress().toByteArray());
        //   result += "\n";
        //   result += "storageBytes:";
        //   result += sellStorageContract.getStorageBytes();
        //   result += "\n";
        //   break;

        case SideChainProposalCreateContract: {
          SideChainProposalCreateContract SideChainProposalCreateContract = contract.getParameter()
              .unpack(SideChainProposalCreateContract.class);
          result += "owner_address: ";
          result += AddressUtil
              .encode58Check(SideChainProposalCreateContract.getOwnerAddress().toByteArray());
          result += "\n";
          result += "parametersMap: ";
          result += SideChainProposalCreateContract.getParametersMap();
          result += "\n";

          break;
        }
        default:
          return "";
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      return "";
    }
    return result;
  }

  public static String printContractList(List<Contract> contractList) {
    String result = "";
    int i = 0;
    for (Contract contract : contractList) {
      result += "contract " + i + " :::";
      result += "\n";
      result += "[";
      result += "\n";
      result += printContract(contract);
      result += "]";
      result += "\n";
      result += "\n";
      i++;
    }
    return result;
  }

  public static String printTransactionRow(Transaction.raw raw) {
    String result = "";

    if (raw.getRefBlockBytes() != null) {
      result += "ref_block_bytes: ";
      result += ByteArray.toHexString(raw.getRefBlockBytes().toByteArray());
      result += "\n";
    }

    if (raw.getRefBlockHash() != null) {
      result += "ref_block_hash: ";
      result += ByteArray.toHexString(raw.getRefBlockHash().toByteArray());
      result += "\n";
    }

    if (raw.getContractCount() > 0) {
      result += "contract: ";
      result += "\n";
      result += "{";
      result += "\n";
      result += printContractList(raw.getContractList());
      result += "}";
      result += "\n";
    }

    result += "timestamp: ";
    result += new Date(raw.getTimestamp());
    result += "\n";

    result += "fee_limit: ";
    result += raw.getFeeLimit();
    result += "\n";

    return result;
  }

  public static String printSignature(List<ByteString> signatureList) {
    String result = "";
    int i = 0;
    for (ByteString signature : signatureList) {
      result += "signature " + i + " :";
      result += ByteArray.toHexString(signature.toByteArray());
      result += "\n";
      i++;
    }
    return result;
  }

  public static String printRet(List<Result> resultList) {
    String results = "";
    int i = 0;
    for (Result result : resultList) {
      results += "result: ";
      results += i;
      results += " ::: ";
      results += "\n";
      results += "[";
      results += "\n";
      results += "code ::: ";
      results += result.getRet();
      results += "\n";
      results += "fee ::: ";
      results += result.getFee();
      results += "\n";
      results += "ContractRet ::: ";
      results += result.getContractRet().name();
      results += "\n";
      results += "]";
      results += "\n";
      i++;
    }
    return results;
  }

  public static String printTransaction(Transaction transaction) {
    String result = "";
    result += "hash: ";
    result += "\n";
    result += ByteArray.toHexString(Sha256Hash.hash(transaction.toByteArray()));
    result += "\n";
    result += "txid: ";
    result += "\n";
    result += ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    result += "\n";

    if (transaction.getRawData() != null) {
      result += "raw_data: ";
      result += "\n";
      result += "{";
      result += "\n";
      result += printTransactionRow(transaction.getRawData());
      result += "}";
      result += "\n";
    }
    if (transaction.getSignatureCount() > 0) {
      result += "signature: ";
      result += "\n";
      result += "{";
      result += "\n";
      result += printSignature(transaction.getSignatureList());
      result += "}";
      result += "\n";
    }
    if (transaction.getRetCount() != 0) {
      result += "ret: ";
      result += "\n";
      result += "{";
      result += "\n";
      result += printRet(transaction.getRetList());
      result += "}";
      result += "\n";
    }
    return result;
  }

  public static String printTransaction(TransactionExtention transactionExtention) {
    String result = "";
    result += "txid: ";
    result += "\n";
    result += ByteArray.toHexString(transactionExtention.getTxid().toByteArray());
    result += "\n";

    Transaction transaction = transactionExtention.getTransaction();
    if (transaction.getRawData() != null) {
      result += "raw_data: ";
      result += "\n";
      result += "{";
      result += "\n";
      result += printTransactionRow(transaction.getRawData());
      result += "}";
      result += "\n";
    }
    if (transaction.getSignatureCount() > 0) {
      result += "signature: ";
      result += "\n";
      result += "{";
      result += "\n";
      result += printSignature(transaction.getSignatureList());
      result += "}";
      result += "\n";
    }
    if (transaction.getRetCount() != 0) {
      result += "ret: ";
      result += "\n";
      result += "{";
      result += "\n";
      result += printRet(transaction.getRetList());
      result += "}";
      result += "\n";
    }
    return result;
  }

  public static String printKey(Key key) {
    StringBuffer result = new StringBuffer();
    result.append("address: ");
    result.append(AddressUtil.encode58Check(key.getAddress().toByteArray()));
    result.append("\n");
    result.append("weight: ");
    result.append(key.getWeight());
    result.append("\n");
    return result.toString();
  }

  public static String printPermissionList(List<Permission> permissionList) {
    String result = "\n";
    result += "[";
    result += "\n";
    int i = 0;
    for (Permission permission : permissionList) {
      result += "permission " + i + " :::";
      result += "\n";
      result += "{";
      result += "\n";
      result += printPermission(permission);
      result += "\n";
      result += "}";
      result += "\n";
      i++;
    }
    result += "]";
    return result;
  }

  public static String printPermission(Permission permission) {
    StringBuffer result = new StringBuffer();
    result.append("permission_type: ");
    result.append(permission.getType());
    result.append("\n");
    result.append("permission_id: ");
    result.append(permission.getId());
    result.append("\n");
    result.append("permission_name: ");
    result.append(permission.getPermissionName());
    result.append("\n");
    result.append("threshold: ");
    result.append(permission.getThreshold());
    result.append("\n");
    result.append("parent_id: ");
    result.append(permission.getParentId());
    result.append("\n");
    result.append("operations: ");
    result.append(ByteArray.toHexString(permission.getOperations().toByteArray()));
    result.append("\n");
    if (permission.getKeysCount() > 0) {
      result.append("keys:");
      result.append("\n");
      result.append("[");
      result.append("\n");
      for (Key key : permission.getKeysList()) {
        result.append(printKey(key));
      }
      result.append("]");
      result.append("\n");
    }
    return result.toString();
  }

  public static boolean priKeyValid(byte[] priKey) {
    if (ArrayUtils.isEmpty(priKey)) {
      return false;
    }
    if (priKey.length != 32) {
      return false;
    }
    //Other rule;
    return true;
  }
}

