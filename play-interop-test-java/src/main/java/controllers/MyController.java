/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
// tag::using-client[]
package controllers;

import example.myapp.helloworld.grpc.GreeterServiceClient;
import example.myapp.helloworld.grpc.HelloRequest;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.mvc.Controller;
import play.mvc.Result;

@Singleton
public class MyController extends Controller {

  private final GreeterServiceClient greeterServiceClient;

  @Inject
  public MyController(GreeterServiceClient greeterServiceClient) {
    this.greeterServiceClient = greeterServiceClient;
  }

  public CompletionStage<Result> sayHello(String name) {
    return greeterServiceClient
        .sayHello(HelloRequest.newBuilder().setName(name).build())
        .thenApply(response -> ok("response: " + response.getMessage()));
  };
}
// end::using-client[]
