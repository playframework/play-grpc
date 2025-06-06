/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.testkit;

import javax.net.ssl.SSLContext;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.grpc.GrpcClientSettings;
import play.api.test.RunningServer;
import play.core.server.ServerEndpoint;
import play.core.server.ServerEndpoints;

/** Helpers to test Java Pekko gRPC clients with Play. */
public final class JavaPekkoGrpcClientHelpers {
  private JavaPekkoGrpcClientHelpers() {}

  /** Creates a GrpcClientSettings from the given NewTestServer. */
  public static GrpcClientSettings grpcClientSettings(final RunningServer runningServer) {
    final ServerEndpoint http2Endpoint = getHttp2Endpoint(runningServer.endpoints());
    return grpcClientSettings(http2Endpoint, runningServer.app().actorSystem());
  }

  /**
   * Unsafely gets the HTTP/2 endpoint from the given ServerEndpoints.
   *
   * <p>If no HTTP/2 endpoint exists this throws an IllegalArgumentException.
   */
  public static ServerEndpoint getHttp2Endpoint(final ServerEndpoints serverEndpoints) {
    final scala.collection.Iterable<ServerEndpoint> possibleEndpoints =
        serverEndpoints
            .endpoints()
            .filter(e -> e.protocols().contains("HTTP/2.0" /* Play's HttpProtocol.HTTP_2_0 */))
            .toIterable();
    if (possibleEndpoints.isEmpty()) {
      throw new IllegalArgumentException(
          String.format(
              "gRPC client can't automatically find HTTP/2 connection: "
                  + "no valid endpoints available. %s",
              serverEndpoints));
    } else if (possibleEndpoints.size() == 1) {
      return possibleEndpoints.head();
    } else {
      // TODO: the decision on which HTTP/2 endpoint to use should be based on config (e.g. maybe
      // the user set
      // `org.apache.pekko.grpc.client."".use-tls` to false for gRPC so this should return the
      // non-TLS HTTP/2
      // endpoint on the list.
      final scala.collection.Iterable<ServerEndpoint> sslEndpoints =
          possibleEndpoints.filter(endpoint -> endpoint.ssl().isDefined()).toIterable();
      return sslEndpoints.head();
    }
  }

  /** Creates a GrpcClientSettings from the given HTTP/2 endpoint and ActorSystem. */
  public static GrpcClientSettings grpcClientSettings(
      final ServerEndpoint http2Endpoint, final ActorSystem actorSystem) {

    final SSLContext sslContext =
        http2Endpoint
            .ssl()
            .getOrElse(
                () -> {
                  throw new IllegalArgumentException(
                      "GrpcClientSettings requires a server endpoint with ssl, but none provided");
                });

    return grpcClientSettings(http2Endpoint, sslContext, actorSystem);
  }

  public static GrpcClientSettings grpcClientSettings(
      final ServerEndpoint http2Endpoint,
      final SSLContext sslContext,
      final ActorSystem actorSystem) {
    return GrpcClientSettings.connectToServiceAt(
            http2Endpoint.host(), http2Endpoint.port(), actorSystem)
        .withSslContext(sslContext);
  }
}
