/*
 * Copyright (C) 2018-2020 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.specs2

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.libs.ws.WSRequest
import play.api.routing.Router
import play.api.test._
import example.myapp.helloworld.grpc.helloworld._
import io.grpc.Status

/**
 * Test for the Play gRPC Specs2 APIs
 */
@RunWith(classOf[JUnitRunner])
class PlaySpecs2Spec extends ForServer with ServerGrpcClient with PlaySpecification with ApplicationFactories {

  protected def applicationFactory: ApplicationFactory =
    withGuiceApp(GuiceApplicationBuilder().overrides(bind[Router].to[GreeterServiceImpl]))

  // RICH: Still need to work out how to make WSClient work properly with endpoints
  def wsUrl(path: String)(implicit running: RunningServer): WSRequest = {
    val ws  = running.app.injector.instanceOf[WSClient]
    val url = running.endpoints.httpEndpoint.get.pathUrl(path)
    ws.url(url)
  }

  "A Play server bound to a gRPC router" should {
    "give a 404 when routing a non-gRPC request" >> { implicit rs: RunningServer =>
      val result = await(wsUrl("/").get)
      result.status must ===(404) // Maybe should be a 426, see #396
    }
    "give an Ok header (and hopefully a not implemented trailer) when routing a non-existent gRPC method" >> {
      implicit rs: RunningServer =>
        val result = await(wsUrl(s"/${GreeterService.name}/FooBar").get)
        result.status must ===(200) // Maybe should be a 426, see #396
      // TODO: Test that trailer has a not implemented status
    }
    "give a grpc-status 13 when routing an empty request to a gRPC method" >> { implicit rs: RunningServer =>
      val result = await(wsUrl(s"/${GreeterService.name}/SayHello").get)
      result.status must ===(200) // Maybe should be a 426, see #396

      // grpc-status 13 means INTERNAL error. See https://developers.google.com/maps-booking/reference/grpc-api/status_codes
      result.header("grpc-status") must beSome(Status.Code.INTERNAL.value().toString)
    }
    "work with a gRPC client" >> { implicit rs: RunningServer =>
      withGrpcClient[GreeterServiceClient] { client: GreeterServiceClient =>
        val reply = await(client.sayHello(HelloRequest("Alice")))
        reply.message must ===("Hello, Alice!")
      }
    }
  }
}
