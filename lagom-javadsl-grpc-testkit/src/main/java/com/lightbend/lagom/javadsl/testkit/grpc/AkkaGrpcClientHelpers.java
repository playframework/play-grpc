/*
 * Copyright (C) 2019 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.testkit.grpc;

import akka.annotation.ApiMayChange;
import akka.grpc.GrpcClientSettings;
import akka.grpc.javadsl.AkkaGrpcClient;
import akka.japi.function.Function;
import akka.japi.function.Function3;
import akka.japi.function.Procedure;
import akka.stream.Materializer;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import scala.concurrent.ExecutionContext;

/** Helpers to test Java Akka gRPC clients with Lagom */
@ApiMayChange
public class AkkaGrpcClientHelpers {

  /**
   * Builds an <code>AkkaGrpcClient</code> for the <code>server</code> run during tests. The <code>
   * server</code> must run with SSL enabled, otherwise the creation of a client will fail.
   *
   * @param server the <code>ServiceTest.TestServer</code> started to run the tests on
   * @param clientFactory a factory method as create by the Akka gRPC code generators
   * @param block user code that given a client will execute the test
   * @param <T>
   * @throws IllegalArgumentException if the <code>server</code> is not setup with SSL enabled
   */
  public static <T extends AkkaGrpcClient> void withGrpcClient(
      ServiceTest.TestServer server,
      Function3<GrpcClientSettings, Materializer, ExecutionContext, T> clientFactory,
      Procedure<T> block)
      throws Exception {
    Function<T, Void> f =
        t -> {
          block.apply(t);
          return null;
        };
    withGrpcClient(server, clientFactory, f);
  }

  /**
   * Builds an <code>AkkaGrpcClient</code> for the <code>server</code> run during tests. The <code>
   * server</code> must run with SSL enabled, otherwise the creation of a client will fail.
   *
   * @param server the <code>ServiceTest.TestServer</code> started to run the tests on
   * @param clientFactory a factory method as create by the Akka gRPC code generators
   * @param block user code that given a client will execute the test
   * @param <T>
   * @throws IllegalArgumentException if the <code>server</code> is not setup with SSL enabled
   */
  public static <T extends AkkaGrpcClient, Result> Result withGrpcClient(
      ServiceTest.TestServer server,
      Function3<GrpcClientSettings, Materializer, ExecutionContext, T> clientFactory,
      Function<T, Result> block)
      throws Exception {

    if (!server.portSsl().isPresent())
      throw new IllegalArgumentException(
          "Creation of a gRPC client is useless. The ServiceTest.TestServer must be setup with SSL enabled.");

    int sslPort = server.portSsl().get();

    GrpcClientSettings settings =
        GrpcClientSettings.connectToServiceAt("127.0.0.1", sslPort, server.system())
            .withSSLContext(server.clientSslContext().get())
            // the authority must match the value of the SSL certificate used in
            // the ServiceTest.TestServer (if/when that changes or is configurable)
            // this value will have to be configurable
            .withOverrideAuthority("localhost");

    T grpcClient = null;
    Result result = null;

    try {
      grpcClient =
          clientFactory.apply(settings, server.materializer(), server.system().dispatcher());
      result = block.apply(grpcClient);
    } finally {
      if (grpcClient != null) {
        grpcClient.close();
      }
    }

    return result;
  }
}
