/*
 * Copyright (C) 2019 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.grpc.interop.test;

import com.lightbend.lagom.javadsl.grpc.interop.GreeterServiceClient;
import com.lightbend.lagom.javadsl.grpc.interop.HelloReply;
import com.lightbend.lagom.javadsl.grpc.interop.HelloRequest;
import com.lightbend.lagom.javadsl.testkit.grpc.AkkaGrpcClientHelpers;
import com.lightbend.lagom.javadsl.grpc.interop.test.api.api.HelloService;
import org.junit.Test;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

public class HelloServiceTest {

  @Test
  public void shouldSayHelloUsingALagomClient() throws Exception {
    withServer(
        defaultSetup(),
        server -> {
          HelloService service = server.client(HelloService.class);

          String msg = service.hello("Alice").invoke().toCompletableFuture().get(5, SECONDS);
          assertEquals("Hi Alice!", msg);
        });
  }

  @Test
  public void shouldSayHelloUsingGrpc() throws Exception {
    withServer(
        defaultSetup().withSsl(),
        server -> {
          // #managed-client
          AkkaGrpcClientHelpers.withGrpcClient(
              server,
              GreeterServiceClient::create,
              serviceClient -> {
                HelloReply reply =
                    serviceClient
                        .sayHello(HelloRequest.newBuilder().setName("Steve").build())
                        .toCompletableFuture()
                        .get(5, SECONDS);
                assertEquals("Hi Steve (gRPC)", reply.getMessage());
              });
          // #managed-client
        });
  }

  @Test
  public void shouldSayHelloUsingUnmanagedGrpc() throws Exception {
    withServer(
        defaultSetup().withSsl(),
        server -> {
          // #unmanaged-client
          GreeterServiceClient client =
              AkkaGrpcClientHelpers.grpcClient(server, GreeterServiceClient::create);
          try {
            HelloReply reply =
                client
                    .sayHello(HelloRequest.newBuilder().setName("Steve").build())
                    .toCompletableFuture()
                    .get(5, SECONDS);
            assertEquals("Hi Steve (gRPC)", reply.getMessage());
          } finally {
            client.close().toCompletableFuture().get(5, SECONDS);
          }
          // #unmanaged-client
        });
  }
}
