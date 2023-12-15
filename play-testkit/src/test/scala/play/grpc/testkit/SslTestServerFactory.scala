/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.testkit

import akka.annotation.ApiMayChange

import play.api.Application
import play.api.test.DefaultTestServerFactory
import play.core.server.ServerConfig

/**
 * A test server factory that configures the server to use SSL.
 */
@ApiMayChange class SslTestServerFactory extends DefaultTestServerFactory {
  override def serverConfig(app: Application): ServerConfig = {
    super
      .serverConfig(app)
      .copy(
        port = None,
        sslPort = Some(0),
      )
  }
}
