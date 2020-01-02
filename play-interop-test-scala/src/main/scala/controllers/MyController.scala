/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
// #using-client
package controllers

import example.myapp.helloworld.grpc.helloworld.GreeterServiceClient
import example.myapp.helloworld.grpc.helloworld.HelloRequest
import javax.inject.Inject
import javax.inject.Singleton
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
class MyController @Inject() (
    implicit greeterClient: GreeterServiceClient,
    cc: ControllerComponents,
    exec: ExecutionContext,
) extends AbstractController(cc) {

  def sayHello(name: String) = Action.async {
    greeterClient
      .sayHello(HelloRequest(name))
      .map { reply =>
        Ok(s"response: ${reply.message}")
      }
  }

}
// #using-client
