package org.tron.sunserver;

import org.tron.api.GrpcAPI.TransactionSignWeight;
import org.tron.protos.Protocol.Transaction;

public interface IMultiTransactionSign {

  Transaction addTransactionSign(Transaction transaction, TransactionSignWeight weight,
      byte[] chainId);

  Transaction setPermissionId(Transaction transaction);
}
