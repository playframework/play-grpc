package play.grpc.testkit

import play.api.Application
import play.api.test.DefaultTestServerFactory
import play.core.server.ServerConfig

class SslTestServerFactory extends DefaultTestServerFactory {
  override def serverConfig(app: Application): ServerConfig = {
    super
      .serverConfig(app)
      .copy(
        port = None,      // disables http port (or use Some(0) for a random port or for a fixed port e.g. 9001 Some(9001))
        sslPort = Some(0),// Using 0 a random port from the operating system will be assigned
      )
  }
}
