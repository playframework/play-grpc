/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
// tag::service-impl[]
package controllers

import javax.inject.Inject
import javax.inject.Singleton

import scala.concurrent.Future

import akka.actor.ActorSystem
import example.myapp.helloworld.grpc.helloworld.AbstractGreeterServiceRouter
import example.myapp.helloworld.grpc.helloworld.HelloReply
import example.myapp.helloworld.grpc.helloworld.HelloRequest

/** User implementation, with support for dependency injection etc */
@Singleton
class GreeterServiceImpl @Inject() (implicit actorSystem: ActorSystem)
    extends AbstractGreeterServiceRouter(actorSystem) {

  override def sayHello(in: HelloRequest): Future[HelloReply] = Future.successful(HelloReply(s"Hello, ${in.name}!"))

}
// end::service-impl[]
