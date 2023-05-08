/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.grpc.interop.test;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.grpc.interop.test.api.api.HelloService;

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
