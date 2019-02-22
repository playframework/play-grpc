package com.lightbend.lagom.scaladsl.grpc.interop.test

import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents


abstract class HelloApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {


  val x = actorSystem
  // Bind the service that this server provides
  override lazy val lagomServer =
    serverFor[HelloService](wire[HelloServiceImpl])
    .additionalRouter(wire[HelloGrpcServiceImpl])

}
