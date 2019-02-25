/*
 * Copyright (C) 2019 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.grpc.interop.test;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.scaladsl.grpc.interop.test.api.HelloService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class HelloServiceImpl implements HelloService {

  @Inject
  public HelloServiceImpl() {}

  @Override
  public ServiceCall<NotUsed, String> hello(String id) {
    return req -> CompletableFuture.completedFuture("Hi " + id + "!");
  }
}
