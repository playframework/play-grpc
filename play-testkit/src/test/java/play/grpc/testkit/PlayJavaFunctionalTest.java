/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.testkit;

import io.grpc.Status;

import akka.grpc.GrpcClientSettings;
import akka.grpc.internal.GrpcProtocolNative;

import example.myapp.helloworld.grpc.*;

import org.junit.*;

import play.*;
import play.api.routing.*;
import play.api.test.*;
import play.inject.guice.*;
import play.libs.ws.*;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static play.inject.Bindings.*;

public final class PlayJavaFunctionalTest {
  private final TestServerFactory testServerFactory = new DefaultTestServerFactory();

  private Application app;
  private RunningServer runningServer;

  private Application provideApplication() {
    return new GuiceApplicationBuilder()
        .overrides(bind(Router.class).to(GreeterServiceImpl.class))
        .build();
  }

  @Before
  public void startServer() throws Exception {
    if (runningServer != null) runningServer.stopServer().close();
    app = provideApplication();
    final play.api.Application app = this.app.asScala();
    runningServer = testServerFactory.start(app);
  }

  @After
  public void stopServer() throws Exception {
    if (runningServer != null) {
      runningServer.stopServer().close();
      runningServer = null;
      app = null;
    }
  }

  private WSResponse wsGet(final String path) throws Exception {
    final WSClient wsClient = app.injector().instanceOf(WSClient.class);
    final String url = runningServer.endpoints().httpEndpoint().get().pathUrl(path);
    return wsClient.url(url).addHeader("Content-Type", GrpcProtocolNative.contentType().toString()).get().toCompletableFuture().get();
  }

  @Test
  public void returns404OnNonGrpcRequest() throws Exception {
    assertEquals(404, wsGet("/").getStatus()); // Maybe should be a 426, see #396
  }

  @Test
  public void returnsGrpcUnimplementedOnNonExistentGrpcMethod() throws Exception {
    final WSResponse rsp = wsGet("/" + GreeterService.name + "/FooBar");
    assertEquals(200, rsp.getStatus());
    assertEquals(
        Integer.toString(Status.Code.UNIMPLEMENTED.value()),
        rsp.getSingleHeader("grpc-status").get());
  }

  @Test
  public void returnsGrpcInvalidArgumentErrorOnEmptyRequestToAGrpcMethod() throws Exception {
    final WSResponse rsp = wsGet("/" + GreeterService.name + "/SayHello");
    assertEquals(200, rsp.getStatus());
    assertEquals(
        Integer.toString(Status.Code.INVALID_ARGUMENT.value()), rsp.getSingleHeader("grpc-status").get());
  }

  @Test
  public void worksWithAGrpcClient() throws Exception {

    final HelloRequest req = HelloRequest.newBuilder().setName("Alice").build();

    final GrpcClientSettings grpcClientSettings =
        JavaAkkaGrpcClientHelpers.grpcClientSettings(runningServer)
            .withOverrideAuthority("localhost");

    final GreeterServiceClient greeterServiceClient =
        GreeterServiceClient.create(
            grpcClientSettings,
            app.asScala().actorSystem());
    try {
      final HelloReply helloReply = greeterServiceClient.sayHello(req).toCompletableFuture().get();
      assertEquals("Hello, Alice!", helloReply.getMessage());
    } finally {
      greeterServiceClient.close().toCompletableFuture().get(30, TimeUnit.SECONDS);
    }
  }
}
