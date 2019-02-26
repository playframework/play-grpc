## Using a gRPC client in Play

Akka gRPC has special support to allow for seamless injection of generated clients in Play. To enable this, you
need first to enable the gRPC plugin as described in the [client docs](https://developer.lightbend.com/docs/akka-grpc/current/client/walkthrough.html) and then add a
source generator in `build.sbt`:

Scala
:  ```
import akka.grpc.gen.scaladsl.play.PlayScalaClientCodeGenerator
akkaGrpcExtraGenerators += PlayScalaClientCodeGenerator
```

Java
:  ```
import akka.grpc.gen.javadsl.play.PlayJavaClientCodeGenerator
akkaGrpcExtraGenerators += PlayJavaClientCodeGenerator
```

This will generate a Play module that provides all generated clients for injection. The module must be enabled
by adding it to the enabled modules in the `application.conf`.

You can then put the following `helloworld.proto` file in `app/protobuf`:

@@snip[helloworld.proto](/play-interop-test-scala/src/main/protobuf/helloworld.proto) { #protoSources }

The module file is generated in @scala[`example.myapp.helloworld.grpc.helloworld.AkkaGrpcClientModule` by default for Scala]
@java[`example.myapp.helloworld.grpc.AkkaGrpcClientModule` by default for Java], which corresponds to the default value
of `flat_package` for @java[Java]@scala[Scala]. You can read more about this in [Services](https://developer.lightbend.com/docs/akka-grpc/current/proto.html).

The exact package of the module will be based on the package the proto files are generated in, configured through
the `java_package` option in the proto-file (if there are multiple different gRPC generated clients the module will
be generated in the longest package prefix shared between the clients).

To hook it into Play, in `application.conf`:

Scala
:   @@snip[application.conf](/play-interop-test-scala/src/main/resources/application.conf) { #client-module-scala }

Java
:   @@snip[application.conf](/play-interop-test-java/src/main/resources/application.conf) { #client-module-java }

The clients are configured with entries under `akka.grpc.client` named after the client (`gRPC` package name dot `ServiceName`),
again, in `application.conf`:

@@snip[application.conf](/play-interop-test-scala/src/main/resources/application.conf) { #service-client-conf }

See [Client Configuration](https://developer.lightbend.com/docs/akka-grpc/current/client/configuration.html) for more information on the available options. If the configuration
is not present for that client and it is used by some other component, the application start will fail with an exception
when injecting the client (see [#271](https://github.com/akka/akka-grpc/issues/271)).

You can now use the client in a controller by injecting it:

Scala
:   @@snip[MyController.scala](/play-interop-test-scala/src/main/scala/controllers/MyController.scala) { #using-client }

Java
:   @@snip[MyController.java](/play-interop-test-java/src/main/java/controllers/MyController.java) { #using-client }
