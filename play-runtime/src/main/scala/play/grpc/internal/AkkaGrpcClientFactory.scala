/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.internal

import scala.language.reflectiveCalls
import scala.reflect.classTag
import scala.reflect.ClassTag

import akka.actor.ClassicActorSystemProvider
import akka.grpc.scaladsl.AkkaGrpcClient
import akka.grpc.GrpcClientSettings

object AkkaGrpcClientFactory {
  def create[T <: AkkaGrpcClient: ClassTag](
      settings: GrpcClientSettings,
  )(implicit sys: ClassicActorSystemProvider): T = {
    // this reflection requires:
    //    object @{service.name}Client {
    //      def apply(settings: GrpcClientSettings)(implicit sys: ClassicActorSystemProvider): @{service.name}Client
    //    }
    val classT: Class[_] = classTag[T].runtimeClass
    val module: AnyRef   = getClass.getClassLoader.loadClass(classT.getName + "$").getField("MODULE$").get(null)
    val instance         =
      module.asInstanceOf[{ def apply(settings: GrpcClientSettings)(implicit sys: ClassicActorSystemProvider): T }]
    instance(settings)(sys)
  }

  /**
   * A function to create an AkkaGrpcClient, bundling its own configuration.
   * These objects are convenient to pass around as implicit values.
   */
  trait Configured[T <: AkkaGrpcClient] {

    /** Create the gRPC client. */
    def create(): T
  }

  /** Bind configuration to a [[AkkaGrpcClientFactory]], creating a [[Configured]]. */
  def configure[T <: AkkaGrpcClient: ClassTag](
      clientSettings: GrpcClientSettings,
  )(implicit sys: ClassicActorSystemProvider): Configured[T] =
    () => AkkaGrpcClientFactory.create[T](clientSettings)
}
