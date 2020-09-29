## Using a gRPC client in Lagom

Akka gRPC has special support to allow for seamless configuration of generated clients in Play. To enable this, you
need first to enable the gRPC plugin as described in the [client docs](https://doc.akka.io/docs/akka-grpc/current/client/walkthrough.html) and then add a
source generator in `build.sbt`:

Scala
:  ```
akkaGrpcExtraGenerators += akka.grpc.gen.scaladsl.play.PlayScalaClientCodeGenerator
```

Java
:  ```
akkaGrpcExtraGenerators += akka.grpc.gen.javadsl.play.PlayJavaClientCodeGenerator
```

This will generate a Play module that provides all generated clients for injection. The module must be enabled
by adding it to the enabled modules in the `application.conf`.

You can then put the following `helloworld.proto` file in `app/protobuf`:

Scala
:   @@snip[helloworld.proto](/lagom-interop-test-scala/src/main/proto/helloworld.proto) { #protoSources }

Java
:   @@snip[helloworld.proto](/lagom-interop-test-java/src/main/proto/helloworld.proto) { #protoSources }


@@@ div { .group-scala }

For Scala, you first need to provide a `GrpcClientSettings` object in your cake:

Scala
:   @@snip[HelloLoader.scala](/lagom-interop-test-scala/src/main/scala/com/lightbend/lagom/scaladsl/grpc/interop/test/HelloLoader.scala) { #service-client-conf }


And then instantiate the client using the settings above:

Scala
:   @@snip[HelloLoader.scala](/lagom-interop-test-scala/src/main/scala/com/lightbend/lagom/scaladsl/grpc/interop/test/HelloLoader.scala) { #service-client-creation }


@@@


@@@ div { .group-java }

For Java, the module file is generated in @java[`com.lightbend.lagom.javadsl.grpc.interop.AkkaGrpcClientModule` by default], which corresponds to the default value
of `flat_package` for @java[Java]@scala[Scala]. You can read more about this in [Services](https://doc.akka.io/docs/akka-grpc/current/proto.html).

The exact package of the module will be based on the package the proto files are generated in, configured through
the `java_package` option in the proto-file (if there are multiple different gRPC generated clients the module will
be generated in the longest package prefix shared between the clients).

To hook it into Play, in `application.conf`:

Java
:   @@snip[application.conf](/lagom-interop-test-java/src/main/resources/application.conf) { #client-module-java }

The clients are configured with entries under `akka.grpc.client` named after the client (`gRPC` package name dot `ServiceName`),
again, in `application.conf`:

Java
:   @@snip[application.conf](/lagom-interop-test-java/src/main/resources/application.conf) { #service-client-conf }

See [Client Configuration](https://doc.akka.io/docs/akka-grpc/current/client/configuration.html) for more information on the available options. If the configuration
is not present for that client and it is used by some other component, the application start will fail with an exception
when injecting the client (see [#271](https://github.com/akka/akka-grpc/issues/271)).

At this point the client is then available to be injected as any other regular object.

@@@
