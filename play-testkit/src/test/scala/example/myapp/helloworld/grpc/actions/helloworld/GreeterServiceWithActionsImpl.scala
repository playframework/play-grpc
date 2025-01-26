/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package example.myapp.helloworld.grpc.actions.helloworld

import scala.concurrent.Future

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer

@Singleton
class GreeterServiceWithActionsImpl @Inject() (implicit mat: Materializer, actorSystem: ActorSystem)
    extends AbstractGreeterServiceRouter(
      actorSystem,
    ) {

  override def sayHello(in: HelloRequest): Future[HelloReply] = {
    actorSystem.log.error("Saying hello!")
    Future.successful(HelloReply(s"Hello, ${in.name}!"))
  }

}
