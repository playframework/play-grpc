/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
package com.example.hello.impl

import akka.actor.ActorSystem
import com.lightbend.lagom.scaladsl.grpc.interop.helloworld.GreeterServiceClient
import com.lightbend.lagom.scaladsl.grpc.interop.helloworld.HelloRequest
import com.lightbend.lagom.scaladsl.grpc.interop.test.HelloApplication
import com.lightbend.lagom.scaladsl.grpc.interop.test.HelloService
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.lightbend.lagom.scaladsl.testkit.grpc.AkkaGrpcClientHelpers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.wordspec.AsyncWordSpec

class HelloServiceAsyncSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server: ServiceTest.TestServer[HelloApplication with LocalServiceLocator] = ServiceTest.startServer(
    ServiceTest.defaultSetup.withSsl(true).withCluster(false),
  ) { ctx =>
    new HelloApplication(ctx) with LocalServiceLocator
  }

  implicit val sys: ActorSystem = server.actorSystem
  val client: HelloService      = server.serviceClient.implement[HelloService]

  // #unmanaged-client
  val grpcClient: GreeterServiceClient = AkkaGrpcClientHelpers.grpcClient(
    server,
    GreeterServiceClient.apply,
  )

  protected override def afterAll(): Unit = {
    grpcClient.close()
    server.stop()
  }

  // #unmanaged-client

  "Hello service (Async)" should {

    "say hello over HTTP" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hi Alice!")
      }
    }

    // #unmanaged-client
    "say hello over gRPC (unmnanaged client)" in {
      grpcClient
        .sayHello(HelloRequest("Alice"))
        .map {
          _.message should be("Hi Alice! (gRPC)")
        }
    }
    // #unmanaged-client

  }

}

class HelloServiceSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll {

  private val server: ServiceTest.TestServer[HelloApplication with LocalServiceLocator] = ServiceTest.startServer(
    ServiceTest.defaultSetup.withSsl(true).withCluster(false),
  ) { ctx =>
    new HelloApplication(ctx) with LocalServiceLocator
  }

  implicit val sys: ActorSystem = server.actorSystem
  implicit val ctx              = server.executionContext
  val client: HelloService      = server.serviceClient.implement[HelloService]

  protected override def afterAll(): Unit = {
    server.stop()
  }

  "Hello service (Sync)" should {

    // #managed-client
    "say hello over gRPC (managed client)" in {
      AkkaGrpcClientHelpers.withGrpcClient(server, GreeterServiceClient.apply _) { grpcClient =>
        grpcClient
          .sayHello(HelloRequest("Alice"))
          .map {
            _.message should be("Hi Alice! (gRPC)")
          }
      }
    }
    // #managed-client

  }

}
