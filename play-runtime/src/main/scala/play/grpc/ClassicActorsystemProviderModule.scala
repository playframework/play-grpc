/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc

import akka.actor.ClassicActorSystemProvider
import play.api.inject.SimpleModule
import play.api.inject.bind
import play.api.libs.concurrent.ActorSystemProvider

/** This should removed when https://github.com/playframework/playframework/pull/10382 is merged/released */
final class ClassicActorsystemProviderModule
    extends SimpleModule(bind[ClassicActorSystemProvider].toProvider[ActorSystemProvider])
