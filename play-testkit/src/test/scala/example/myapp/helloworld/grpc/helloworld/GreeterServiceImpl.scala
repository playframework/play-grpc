/*
 * Copyright (C) 2018-2020 Lightbend Inc. <https://www.lightbend.com>
 */
package example.myapp.helloworld.grpc.helloworld

import akka.actor.ActorSystem
import akka.stream.Materializer
import javax.inject.Inject
import javax.inject.Singleton

import scala.concurrent.Future

/** User implementation, with support for dependency injection etc */
@Singleton
class GreeterServiceImpl @Inject() (implicit mat: Materializer, actorSystem: ActorSystem)
    extends AbstractGreeterServiceRouter(mat, actorSystem) {

  override def sayHello(in: HelloRequest): Future[HelloReply] = {
    actorSystem.log.error("Saying hello!")
    Future.successful(HelloReply(s"Hello, ${in.name}!"))
  }

}
// #service-impl
