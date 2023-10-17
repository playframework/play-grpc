/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.specs2

import scala.reflect.ClassTag

import akka.grpc.scaladsl.AkkaGrpcClient
import play.api.test.RunningServer
import play.grpc.internal.AkkaGrpcClientFactory
import play.grpc.testkit.AkkaGrpcClientHelpers

/**
 * Helpers to test gRPC clients with Play using Specs2.
 *
 * Mixes a method into [[AkkaGrpcClientHelpers]] that knows how to configure
 * gRPC clients for the running server.
 */
trait ServerGrpcClient extends AkkaGrpcClientHelpers {

  /** Configure the factory by combining the app and the current implicit server information */
  implicit def configuredAkkaGrpcClientFactory[T <: AkkaGrpcClient: ClassTag](
      implicit running: RunningServer,
  ): AkkaGrpcClientFactory.Configured[T] = {
    AkkaGrpcClientHelpers.factoryForAppEndpoints(running.app, running.endpoints)
  }

}
