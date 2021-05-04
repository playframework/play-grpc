/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
package example.myapp.helloworld.grpc.actions.helloworld

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.stream.Materializer
import javax.inject.Inject
import javax.inject.Singleton
import play.api.mvc.DefaultActionBuilder
import play.api.mvc.PlayBodyParsers

@Singleton
class GreeterServiceWithActionsImpl @Inject() (
    implicit
    mat: Materializer,
    actorSystem: ActorSystem,
    parsers: PlayBodyParsers,
    actionBuilder: DefaultActionBuilder,
) extends AbstractGreeterServiceRouter(
      actorSystem
    ) {

  override def sayHello(in: HelloRequest): Future[HelloReply] = {
    actorSystem.log.error("Saying hello!")
    Future.successful(HelloReply(s"Hello, ${in.name}!"))
  }

}
