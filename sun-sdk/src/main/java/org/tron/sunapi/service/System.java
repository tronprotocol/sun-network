package org.tron.sunapi.service;

import org.tron.api.GrpcAPI.BlockListExtention;
import org.tron.api.GrpcAPI.NodeList;
import org.tron.api.GrpcAPI.NumberMessage;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.sunapi.SunNetworkResponse;

public interface System {
  SunNetworkResponse<NodeList> listNodes();

  SunNetworkResponse<Block> getBlock(long blockNum);

  SunNetworkResponse<Long> getTransactionCountByBlockNum(long blockNum);

  SunNetworkResponse<String> getNextMaintenanceTime();

  SunNetworkResponse<NumberMessage> getTotalTransaction();

  SunNetworkResponse<Transaction> getTransactionById(String txid);

  SunNetworkResponse<TransactionInfo> getTransactionInfoById(String trxId);

  SunNetworkResponse<Block> getBlockById(String blockID);

  SunNetworkResponse<BlockListExtention> getBlockByLimitNext(long start, long end);
}
