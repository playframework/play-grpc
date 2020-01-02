/*
 * Copyright (C) 2019-2020 Lightbend Inc. <https://www.lightbend.com>
 */
// #service-impl
package com.lightbend.lagom.javadsl.grpc.interop.test.api;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.lightbend.lagom.javadsl.grpc.interop.AbstractGreeterServiceRouter;
import com.lightbend.lagom.javadsl.grpc.interop.HelloReply;
import com.lightbend.lagom.javadsl.grpc.interop.HelloRequest;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public class HelloGrpcServiceImpl extends AbstractGreeterServiceRouter {

  @Inject
  public HelloGrpcServiceImpl(Materializer mat, ActorSystem sys) {
    super(mat, sys);
  }

  @Override
  public CompletionStage<HelloReply> sayHello(HelloRequest in) {
    HelloReply reply = HelloReply.newBuilder().setMessage("Hi " + in.getName() + " (gRPC)").build();
    return CompletableFuture.completedFuture(reply);
  }
}
// #service-impl
