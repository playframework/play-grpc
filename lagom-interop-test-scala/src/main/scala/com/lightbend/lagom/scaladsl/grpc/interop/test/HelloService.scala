/*
 * Copyright (C) 2019-2020 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.grpc.interop.test

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.api.ServiceCall

/**
 * The Hello service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the HelloService.
 */
trait HelloService extends Service {

  /**
   * Example: curl http://localhost:9000/api/hello/Alice
   */
  def hello(id: String): ServiceCall[NotUsed, String]

  final override def descriptor = {
    import Service._
    named("hello-srvc")
      .withCalls(
        pathCall("/api/hello/:id", hello _),
      )
      .withAutoAcl(true)
  }
}
