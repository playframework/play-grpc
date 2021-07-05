## Serving gRPC from Lagom

To be able to serve gRPC from a Lagom application you must enable [Play HTTP/2 Support](https://www.playframework.com/documentation/2.8.x/AkkaHttpServer#HTTP%2F2-support-%28experimental%29)
with [HTTPS](https://www.playframework.com/documentation/2.8.x/ConfiguringHttps) and the ALPN agent.

After @ref[adding Akka gRPC sbt plugin](overview.md) you need to enable the Play server side code generator in `build.sbt`:

Scala
:   ```scala
akkaGrpcExtraGenerators += play.grpc.gen.scaladsl.PlayScalaServerCodeGenerator
```

Java
:   ```scala
akkaGrpcExtraGenerators += play.grpc.gen.javadsl.PlayJavaServerCodeGenerator
```

The plugin will look for `.proto` service descriptors in `src/main/protobuf` and output an abstract class per service
that you then implement, so for example for the following protobuf descriptor:

Scala
:   @@snip[helloworld.proto](/lagom-interop-test-scala/src/main/proto/helloworld.proto) { #protoSources }

Java
:   @@snip[helloworld.proto](/lagom-interop-test-java/src/main/proto/helloworld.proto) { #protoSources }

You will get an abstract class named @scala[`com.lightbend.lagom.scaladsl.grpc.interop.AbstractGreeterServiceRouter`]
@java[`com.lightbend.lagom.javadsl.grpc.interop.AbstractGreeterServiceRouter`]. Create a concrete subclass implementing this 
wherever you see fit in your project, let's say @scala[`com.lightbend.lagom.scaladsl.grpc.interop.test.HelloGrpcServiceImpl`]@java[`com.lightbend.lagom.javadsl.grpc.interop.test.api.HelloGrpcServiceImpl`] like so:

Scala
:   @@snip[HelloGrpcServiceImpl.scala](/lagom-interop-test-scala/src/main/scala/com/lightbend/lagom/scaladsl/grpc/interop/test/HelloGrpcServiceImpl.scala) { #service-impl }

Java
:   @@snip[HelloGrpcServiceImpl.java](/lagom-interop-test-java/src/main/java/com/lightbend/lagom/javadsl/grpc/interop/test/api/HelloGrpcServiceImpl.java) { #service-impl }


And then, you need to bind the new additional router in your project.

@@@ div { .group-scala }

In Scala, you use `additionalRouter` method when creating your `lagomServer` instance:

Scala
:   @@snip[HelloLoader.scala](/lagom-interop-test-scala/src/main/scala/com/lightbend/lagom/scaladsl/grpc/interop/test/HelloLoader.scala) { #service-additional-router }

@@@

@@@ div { .group-java }

In Java, you need to bind the service with the additional router:

Java
:   @@snip[HelloModule.java](/lagom-interop-test-java/src/main/java/HelloModule.java) { #service-additional-router }

And finally enable this module in your `application.conf` file:

Java
:   @@snip[application.conf](/lagom-interop-test-java/src/main/resources/application.conf) { #service-module }


@@@

A gRPC client can now connect to the server and call the provided services.
