# Lagom gRPC support overview

## gRPC

[gRPC](https://grpc.io/) is a transport mechanism for request/response and (non-persistent) streaming use cases. See [Akka gRPC documentation for an explanation of why gRPC](https://doc.akka.io/docs/akka-grpc/current/whygrpc.html) and when to use it as your transport.

## How to add the sbt plugin

To get started, for both client and server code generation, you will need to add Akka gRPC sbt plugin:

sbt
:   @@@vars
    ```scala
    // in project/plugins.sbt:
    addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "$akka.grpc.version$")
    libraryDependencies += "com.lightbend.play" %% "play-grpc-generators" % "$project.version$"
    ```
    @@@ 


And then in your `build.sbt` file, you will need to enable the plugin for the project that contains the `.proto` files, and configure the language used in project:

Scala
:   @@@vars
    ```scala
    // in build.sbt:
    import play.grpc.gen.scaladsl.PlayScalaServerCodeGenerator

    lazy val `greeter-service-impl` = (project in file("greeter-impl"))
        .enablePlugins(LagomScala)
        .enablePlugins(AkkaGrpcPlugin)
        // enables serving HTTP/2
        .enablePlugins(PlayAkkaHttp2Support)
        .settings(
            // Using Scala
            akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
            akkaGrpcGeneratedSources := Seq( AkkaGrpc.Server, AkkaGrpc.Client ),
            akkaGrpcExtraGenerators in Compile += PlayScalaServerCodeGenerator
        )
    ```
    @@@
    
Java
:   @@@vars
    ```scala
    // in build.sbt:
    import play.grpc.gen.javadsl.PlayJavaServerCodeGenerator

    lazy val `greeter-service-impl` = (project in file("greeter-impl"))
        .enablePlugins(LagomScala)
        .enablePlugins(AkkaGrpcPlugin)
        // enables serving HTTP/2
        .enablePlugins(PlayAkkaHttp2Support)
        .settings(
            // Using Java
            akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Java),
            akkaGrpcGeneratedSources := Seq( AkkaGrpc.Server, AkkaGrpc.Client ),
            akkaGrpcExtraGenerators in Compile += PlayJavaServerCodeGenerator
        )
    ```
    @@@

After that, you should be able to generate and use the code to @ref[serve](serving-grpc.md) or @ref[consume](consuming-grpc.md) gRPC services.
