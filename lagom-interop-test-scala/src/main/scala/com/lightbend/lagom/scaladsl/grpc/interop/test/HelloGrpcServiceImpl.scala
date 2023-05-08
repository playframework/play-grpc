/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
// #service-impl
package com.lightbend.lagom.scaladsl.grpc.interop.test

import scala.concurrent.Future

import akka.actor.ActorSystem
import com.lightbend.lagom.scaladsl.grpc.interop.helloworld.AbstractGreeterServiceRouter
import com.lightbend.lagom.scaladsl.grpc.interop.helloworld.HelloReply
import com.lightbend.lagom.scaladsl.grpc.interop.helloworld.HelloRequest

class HelloGrpcServiceImpl(sys: ActorSystem) extends AbstractGreeterServiceRouter(sys) {
  override def sayHello(in: HelloRequest): Future[HelloReply] =
    Future.successful(HelloReply(s"Hi ${in.name}! (gRPC)"))
}
// #service-impl
