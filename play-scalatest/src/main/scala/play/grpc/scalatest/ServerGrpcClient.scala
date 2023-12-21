/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.scalatest

import scala.reflect.ClassTag

import org.apache.pekko.grpc.scaladsl.PekkoGrpcClient
import org.scalatest.TestData
import org.scalatestplus.play.BaseOneServerPerTest
import play.api.test.DefaultTestServerFactory
import play.api.test.RunningServer
import play.api.Application
import play.grpc.internal.PekkoGrpcClientFactory
import play.grpc.testkit.PekkoGrpcClientHelpers

/**
 * Helpers to test gRPC clients with Play using ScalaTest.
 *
 * Mixes a method into [[PekkoGrpcClientHelpers]] that knows how to configure
 */
trait ServerGrpcClient extends PekkoGrpcClientHelpers { this: BaseOneServerPerTest =>

  /** Configure the factory by combining the current app and server information */
  implicit def configuredPekkoGrpcClientFactory[T <: PekkoGrpcClient: ClassTag](
      implicit running: RunningServer,
  ): PekkoGrpcClientFactory.Configured[T] = {
    PekkoGrpcClientHelpers.factoryForAppEndpoints(running.app, running.endpoints)
  }
}
