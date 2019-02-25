package com.lightbend.lagom.scaladsl.grpc.interop.test

import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.grpc.interop.helloworld.AbstractGreeterServiceRouter
import com.lightbend.lagom.scaladsl.grpc.interop.helloworld.HelloReply
import com.lightbend.lagom.scaladsl.grpc.interop.helloworld.HelloRequest

import scala.concurrent.Future

class HelloGrpcServiceImpl(mat: Materializer) extends AbstractGreeterServiceRouter(mat) {
  override def sayHello(in: HelloRequest): Future[HelloReply] =
    Future.successful(HelloReply(s"Hi ${in.name}! (gRPC)"))
}
