## Gradle support in Play

To enable the Play support in a Gradle project you need to set the option `generatePlay` to true.
This will make sure play-specific code is generated in addition to plain Akka gRPC code:

```
akkaGrpc {
  language = "Java"
  generateClient = true
  generateServer = true
  generatePlay = true
}
```

For a full example project see the
@java[[Quickstart project](https://github.com/playframework/play-java-grpc-example).]
@scala[[Quickstart project](https://github.com/playframework/play-scala-grpc-example).]

See the [gradle support docs](https://developer.lightbend.com/docs/akka-grpc/current/buildtools/gradle.html) for details about the other options.
