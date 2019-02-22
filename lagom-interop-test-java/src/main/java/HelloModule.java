import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.lagom.scaladsl.grpc.interop.test.HelloGrpcServiceImpl;
import com.lightbend.lagom.scaladsl.grpc.interop.test.HelloServiceImpl;
import com.lightbend.lagom.scaladsl.grpc.interop.test.api.HelloService;

public class HelloModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(
        HelloService.class, HelloServiceImpl.class, additionalRouter(HelloGrpcServiceImpl.class));
  }
}
