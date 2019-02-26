/*
 * Copyright (C) 2019 Lightbend Inc. <https://www.lightbend.com>
 */
package scala.com.example.hello.impl

import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.grpc.interop.helloworld.GreeterServiceClient
import com.lightbend.lagom.scaladsl.grpc.interop.helloworld.HelloRequest
import com.lightbend.lagom.scaladsl.grpc.interop.test.HelloApplication
import com.lightbend.lagom.scaladsl.grpc.interop.test.HelloService
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.lightbend.lagom.scaladsl.testkit.grpc.AkkaGrpcClientHelpers
import org.scalatest.AsyncWordSpec
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Matchers

class HelloServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server: ServiceTest.TestServer[HelloApplication with LocalServiceLocator] = ServiceTest.startServer(
    ServiceTest.defaultSetup.withSsl(true),
  ) { ctx =>
    new HelloApplication(ctx) with LocalServiceLocator
  }

  val client: HelloService = server.serviceClient.implement[HelloService]
  val grpcClient: GreeterServiceClient = AkkaGrpcClientHelpers.grpcClient(
    server,
    GreeterServiceClient.apply,
  )

  implicit val mat: Materializer = server.materializer

  protected override def afterAll(): Unit = {
    grpcClient.close()
    server.stop()
  }

  "Hello service" should {

    "say hello over HTTP" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hi Alice!")
      }
    }

    "say hello over gRPC" in {
      grpcClient
        .sayHello(HelloRequest("Alice"))
        .map {
          _.message should be("Hi Alice! (gRPC)")
        }
    }

  }
}
