/*
 * Copyright (C) 2019 Lightbend Inc. <https://www.lightbend.com>
 */
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.lagom.javadsl.grpc.interop.test.api.HelloGrpcServiceImpl;
import com.lightbend.lagom.javadsl.grpc.interop.test.HelloServiceImpl;
import com.lightbend.lagom.javadsl.grpc.interop.test.api.api.HelloService;

public class HelloModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(
        HelloService.class, HelloServiceImpl.class, additionalRouter(HelloGrpcServiceImpl.class));
  }
}
