## Serving gRPC from Play

To be able to serve gRPC from a Play Framework app you must enable [HTTP/2 Support](https://www.playframework.com/documentation/2.7.x/AkkaHttpServer#HTTP%2F2-support-%28experimental%29)
with HTTPS and the ALPN agent. This is still somewhat involved and we hope to simplify it.

@@@ warning
  To use gRPC in Play Framework you must enable [HTTP/2 Support](https://www.playframework.com/documentation/2.7.x/AkkaHttpServer#HTTP%2F2-support-%28experimental%29).
@@@

Generating classes from the gRPC service definition is done by adding the [Akka gRPC plugin to your sbt build](https://developer.lightbend.com/docs/akka-grpc/current/buildtools/sbt.html) along with the Play gRPC generators:

sbt
:   @@@vars
    ```scala
    // in project/plugins.sbt:
    addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "$akka.grpc.version$")
    resolvers += Resolver.bintrayRepo("playframework", "maven")
    libraryDependencies += "com.lightbend.play" %% "play-grpc-generators" % "$project.version$"
    ```
    @@@


Then you need to enable the Play server side code generator in `build.sbt`:

Scala
:   @@@vars
    ```scala
    enablePlugins(AkkaGrpcPlugin)
    import play.grpc.gen.scaladsl.PlayScalaServerCodeGenerator
    akkaGrpcExtraGenerators += PlayScalaServerCodeGenerator
    libraryDependencies += "com.lightbend.play" %% "play-grpc-runtime" % "$project.version$"
    ```
    @@@

Java
:   @@@vars
    ```scala
    enablePlugins(AkkaGrpcPlugin)
    import play.grpc.gen.javadsl.PlayJavaServerCodeGenerator
    akkaGrpcExtraGenerators += PlayJavaServerCodeGenerator
    libraryDependencies += "com.lightbend.play" %% "play-grpc-runtime" % "$project.version$"
    ```
    @@@

The plugin will look for `.proto` service descriptors in `app/protobuf` and output an abstract class per service
that you then implement, so for example for the following protobuf descriptor:

@@snip[helloworld.proto](/play-interop-test-scala/src/main/proto/helloworld.proto) { #protoSources }

You will get an abstract class named @scala[`example.myapp.helloworld.grpc.helloworld.AbstractGreeterServiceRouter`]
@java[`example.myapp.helloworld.grpc.AbstractGreeterServiceRouter`]Create a concrete subclass implementing this 
wherever you see fit in your project, let's say `controller.GreeterServiceImpl` like so:

Scala
:   @@snip[GreeterServiceImpl.scala](/play-interop-test-scala/src/main/scala/controllers/GreeterServiceImpl.scala) { #service-impl }

Java
:   @@snip[GreeterServiceImpl.java](/play-interop-test-java/src/main/java/controllers/GreeterServiceImpl.java) { #service-impl }

And then add the router to your Play `conf/routes` file. Note that the router already knows its own path since it is
based on the package name and service name of the service and therefore the path `/` is enough to get it to end up in the right place
(in this example the path will be `/helloworld.GreeterService`).
It cannot be added at an arbitrary path (if you try to do so an exception will be thrown when the router is started).

```
->     /   controllers.GreeterServiceImpl
```

A gRPC client can now connect to the server and call the provided services.
