/*
 * Copyright (C) 2018-2020 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.scalatest

import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.routing.Router
import io.grpc.Status
import example.myapp.helloworld.grpc.helloworld._

/**
 * Test for the Play gRPC ScalaTest APIs
 */
class PlayScalaTestSpec
    extends PlaySpec
    with GuiceOneServerPerTest
    with ServerGrpcClient
    with ScalaFutures
    with IntegrationPatience {

  override def fakeApplication(): Application = {
    GuiceApplicationBuilder()
      .overrides(bind[Router].to[GreeterServiceImpl])
      .build()
  }

  implicit def ws: WSClient = app.injector.instanceOf(classOf[WSClient])

  "A Play server bound to a gRPC router" must {
    "give a 404 when routing a non-gRPC request" in {
      val result = wsUrl("/").get.futureValue
      result.status must be(404) // Maybe should be a 426, see #396
    }
    "give a grpc 'unimplemented' error when routing a non-existent gRPC method" in {
      val result = wsUrl(s"/${GreeterService.name}/FooBar").get.futureValue
      result.status must be(200) // Maybe should be a 426, see #396
      result.header("grpc-status") mustEqual Some(Status.Code.UNIMPLEMENTED.value().toString)
    }
    "give a grpc 'internal' error when routing an empty request to a gRPC method" in {
      val result = wsUrl(s"/${GreeterService.name}/SayHello").get.futureValue
      result.status must be(200)
      result.header("grpc-status") mustEqual Some(Status.Code.INTERNAL.value().toString)
    }
    "work with a gRPC client" in withGrpcClient[GreeterServiceClient] { client: GreeterServiceClient =>
      val reply = client.sayHello(HelloRequest("Alice")).futureValue
      reply.message must be("Hello, Alice!")
    }
  }
}
