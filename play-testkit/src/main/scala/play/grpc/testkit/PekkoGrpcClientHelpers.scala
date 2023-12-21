/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.testkit

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.grpc.scaladsl.PekkoGrpcClient
import org.apache.pekko.stream.Materializer
import play.api.Application
import play.core.server.ServerEndpoint
import play.core.server.ServerEndpoints
import play.grpc.internal.PekkoGrpcClientFactory

/**
 * Helpers to test gRPC clients with Play. The methods in this class require
 * an implicit `PekkoGrpcClientFactory.Configured[T]` to be in scope. This can
 * usually be done by mixing in a method that knows how to configure the factory
 * for the current environment, e.g. by configuring the correct port values.
 */
trait PekkoGrpcClientHelpers {

  /**
   * Create a gRPC client to connect to the currently running test server.
   * @tparam T The type of client to create.
   * @return
   */
  def withGrpcClient[T <: PekkoGrpcClient]: WithGrpcClient[T] = new WithGrpcClient[T]

  /**
   * Runs a block of code with a gRPC client, closing the client afterwards.
   * @tparam T The type of gRPC client.
   */
  final class WithGrpcClient[T <: PekkoGrpcClient] {
    def apply[U](f: T => U)(implicit factory: PekkoGrpcClientFactory.Configured[T]): U = {
      val client = grpcClient[T]
      try f(client)
      finally {
        Await.result(client.close(), grpcClientCloseTimeout)
        ()
      }
    }
  }

  /**
   * Get a gRPC client to connect to the currently running test server. Remember
   * to close it afterwards, or use [[withGrpcClient]] to have it closed automatically.
   */
  def grpcClient[T <: PekkoGrpcClient](implicit factory: PekkoGrpcClientFactory.Configured[T]): T =
    factory.create()

  /** The close timeout used by gRPC clients. */
  protected def grpcClientCloseTimeout: Duration = Duration(30, TimeUnit.SECONDS)

}

object PekkoGrpcClientHelpers {

  /**
   * Configure a factory from an application and some server endpoints. Expects to have exactly one HTTP/2 endpoint.
   */
  def factoryForAppEndpoints[T <: PekkoGrpcClient: ClassTag](
      app: Application,
      serverEndpoints: ServerEndpoints,
  ): PekkoGrpcClientFactory.Configured[T] = {
    factoryForAppEndpoints(app, JavaPekkoGrpcClientHelpers.getHttp2Endpoint(serverEndpoints))
  }

  /**
   * Configure a factory from an application and a server endpoints.
   */
  def factoryForAppEndpoints[T <: PekkoGrpcClient: ClassTag](
      app: Application,
      serverEndpoint: ServerEndpoint,
  ): PekkoGrpcClientFactory.Configured[T] = {
    implicit val sys: ActorSystem                   = app.actorSystem
    implicit val materializer: Materializer         = app.materializer
    implicit val executionContext: ExecutionContext = sys.dispatcher
    PekkoGrpcClientFactory.configure[T](
      JavaPekkoGrpcClientHelpers
        .grpcClientSettings(serverEndpoint, sys)
        .withOverrideAuthority("localhost"),
    )
  }
}
