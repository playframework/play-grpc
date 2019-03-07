/*
 * Copyright (C) 2019 Lightbend Inc. <https://www.lightbend.com>
 */
// #service-additional-router
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.lagom.javadsl.grpc.interop.test.api.HelloGrpcServiceImpl;
import com.lightbend.lagom.javadsl.grpc.interop.test.HelloServiceImpl;
import com.lightbend.lagom.javadsl.grpc.interop.test.api.api.HelloService;

public class HelloModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(
        // bind the lagom service
        HelloService.class, HelloServiceImpl.class,
        // include additional routers (in this case a gRPC router)
        additionalRouter(HelloGrpcServiceImpl.class)
    );
  }
}
// #service-additional-router
