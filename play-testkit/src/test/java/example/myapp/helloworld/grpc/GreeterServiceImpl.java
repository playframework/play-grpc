/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package example.myapp.helloworld.grpc;

import akka.actor.ActorSystem;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.inject.Singleton;

/** User implementation, with support for dependency injection etc */
@Singleton
public class GreeterServiceImpl extends AbstractGreeterServiceRouter {

  @Inject
  public GreeterServiceImpl(final ActorSystem actorSystem) {
    super(actorSystem);
  }

  @Override
  public CompletionStage<HelloReply> sayHello(final HelloRequest in) {
    final String message = String.format("Hello, %s!", in.getName());
    final HelloReply reply = HelloReply.newBuilder().setMessage(message).build();
    return CompletableFuture.completedFuture(reply);
  }
}
