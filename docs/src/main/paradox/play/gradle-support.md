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

See the [Akka gRPC Gradle support docs](https://doc.akka.io/docs/akka-grpc/current/buildtools/gradle.html) for further details.
