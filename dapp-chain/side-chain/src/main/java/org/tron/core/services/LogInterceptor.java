package org.tron.core.services;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogInterceptor implements ServerInterceptor
{

  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
      ServerCallHandler<ReqT, RespT> next) {
    try {
      logger.info("gRPC " + headers.toString() + call.getMethodDescriptor());
    }
    catch (Exception e) {

    }
    return next.startCall(call, headers);
  }
}
