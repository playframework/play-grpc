## Using a gRPC client in Lagom tests

When your Lagom Service exposes a gRPC interface you will need a gRPC Client on your 
tests. `play-grpc` provides extensions to @java[[Lagom's `testkit`](https://www.lagomframework.com/documentation/current/java/Test.html#How-to-test-one-service)]@scala[[Lagom's `testkit`](https://www.lagomframework.com/documentation/current/scala/TestingServices.html#How-to-test-one-service)]. 

First you have to include the dependency to the testkit extension:

Scala
:  ```scala
"com.lightbend.play" %% "lagom-scaladsl-grpc-testkit" % version
```

Java
:  ```scala
"com.lightbend.play" %% "lagom-javadsl-grpc-testkit" % version
```

### Unmanaged client

You can use an unmanaged client with an idiom similar to Lagom's @java[[`server.client`](https://www.lagomframework.com/documentation/current/java/Test.html#How-to-test-one-service)]@scala[[`server.client`](https://www.lagomframework.com/documentation/current/scala/TestingServices.html#How-to-test-one-service)]. The main difference is that the gRPC unmanaged client requires manual 
resource cleanup so you are responsible for invoking `close` once you are done using the client instance. This option is convenient if you want to reuse the client in multiple tests or even within the same test.

Scala
:   @@snip [HelloServiceSpec.scala](../../../../lagom-interop-test-scala/src/test/scala/scala/com/example/hello/impl/HelloServiceSpec.scala)  { #unmanaged-client }

Java
:   @@snip [HelloServiceTest.java](../../../../lagom-interop-test-java/src/test/java/com/lightbend/lagom/javadsl/grpc/interop/test/HelloServiceTest.java)  { #unmanaged-client }

### Managed client

The Lagom gRPC managed client is a similar API but will handle the resource management for you. This option is convenient if you want a fresh new isntance of the client on each use, but is also more costly:

Scala
:   @@snip [HelloServiceSpec.scala](../../../../lagom-interop-test-scala/src/test/scala/scala/com/example/hello/impl/HelloServiceSpec.scala)  { #managed-client }

Java
:   @@snip [HelloServiceTest.java](../../../../lagom-interop-test-java/src/test/java/com/lightbend/lagom/javadsl/grpc/interop/test/HelloServiceTest.java)  { #managed-client }
