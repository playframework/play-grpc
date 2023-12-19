/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
// tag::using-client[]
package controllers

import javax.inject.Inject
import javax.inject.Singleton

import scala.concurrent.ExecutionContext

import example.myapp.helloworld.grpc.helloworld.GreeterServiceClient
import example.myapp.helloworld.grpc.helloworld.HelloRequest
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents

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
// end::using-client[]
