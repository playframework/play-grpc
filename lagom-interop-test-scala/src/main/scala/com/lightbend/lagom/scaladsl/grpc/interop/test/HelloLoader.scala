/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.grpc.interop.test

import java.time.Duration

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import com.lightbend.lagom.scaladsl.grpc.interop.GreeterService
import com.lightbend.lagom.scaladsl.grpc.interop.helloworld.GreeterServiceClient
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

import scala.concurrent.ExecutionContextExecutor

abstract class HelloApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with AhcWSComponents {

  // #service-additional-router
  // Bind the service that this server provides
  override lazy val lagomServer =
    serverFor[HelloService](wire[HelloServiceImpl])
      .additionalRouter(wire[HelloGrpcServiceImpl])
  // #service-additional-router

  // #service-client-conf
  // Implicits required by GrpcClientSettings
  private implicit val dispatcher: ExecutionContextExecutor = actorSystem.dispatcher
  private implicit val sys: ActorSystem                     = actorSystem

  private lazy val settings = GrpcClientSettings
    .usingServiceDiscovery(GreeterService.name)
    .withServicePortName("https")
    // response timeout
    .withDeadline(Duration.ofSeconds(5))
    // use a small reconnectionAttempts value to
    // cause a client reload in case of failure
    .withConnectionAttempts(5)
  // #service-client-conf

  // #service-client-creation
  lazy val greeterServiceClient: GreeterServiceClient = GreeterServiceClient(settings)
  // #service-client-creation

}
