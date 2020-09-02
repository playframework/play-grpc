/*
 * Copyright (C) 2018-2019 Lightbend Inc. <https://www.lightbend.com>
 */
package example.myapp.helloworld.grpc.actions.helloworld

import javax.inject.Inject
import javax.inject.Singleton
import akka.stream.Materializer
import akka.actor.ActorSystem
import play.api.mvc.PlayBodyParsers
import play.api.mvc.DefaultActionBuilder

import scala.concurrent.Future

@Singleton
class GreeterServiceWithActionsImpl @Inject() (
    implicit
    mat: Materializer,
    actorSystem: ActorSystem,
    parsers: PlayBodyParsers,
    actionBuilder: DefaultActionBuilder,
) extends AbstractGreeterServiceRouter(
      mat,
      actorSystem,
      parsers,
      actionBuilder,
    ) {

  override def sayHello(in: HelloRequest): Future[HelloReply] = {
    actorSystem.log.error("Saying hello!")
    Future.successful(HelloReply(s"Hello, ${in.name}!"))
  }

}
