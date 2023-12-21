/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.specs2

import scala.reflect.ClassTag

import org.apache.pekko.grpc.scaladsl.PekkoGrpcClient
import play.api.test.RunningServer
import play.grpc.internal.PekkoGrpcClientFactory
import play.grpc.testkit.PekkoGrpcClientHelpers

/**
 * Helpers to test gRPC clients with Play using Specs2.
 *
 * Mixes a method into [[PekkoGrpcClientHelpers]] that knows how to configure
 * gRPC clients for the running server.
 */
trait ServerGrpcClient extends PekkoGrpcClientHelpers {

  /** Configure the factory by combining the app and the current implicit server information */
  implicit def configuredPekkoGrpcClientFactory[T <: PekkoGrpcClient: ClassTag](
      implicit running: RunningServer,
  ): PekkoGrpcClientFactory.Configured[T] = {
    PekkoGrpcClientHelpers.factoryForAppEndpoints(running.app, running.endpoints)
  }

}
