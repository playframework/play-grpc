package build.play.grpc

import sbt._
import sbt.Keys._

import akka.grpc.gen.{ BuildInfo => AkkaGrpcBuildInfo }

object Dependencies {

  object Versions {
    val scala212 = "2.12.19"
    val scala213 = "2.13.14"
    val scala3   = "3.3.3"

    // Don't use AkkaGrpcBuildInfo.akkaHttpVersion or AkkaGrpcBuildInfo.akkaVersion and prioritize
    // aligning with versions transitively brought in via Play.
    def akka(scalaVersion: String) = if (CrossVersion.binaryScalaVersion(scalaVersion) == "3") "2.7.0" else "2.6.21"
    // bumps Akka HTTP version beyond play's 10.1.x
    def akkaHttp(scalaVersion: String) =
      if (CrossVersion.binaryScalaVersion(scalaVersion) == "3") "10.5.0" else "10.2.10"

    def conf(scalaVersion: String) =
      if (CrossVersion.binaryScalaVersion(scalaVersion) == "3") Some(Provided.name) else None

    val akkaGrpc: String = AkkaGrpcBuildInfo.version
    val grpc: String     = AkkaGrpcBuildInfo.grpcVersion

    val play = "2.9.3"

    val scalaTest         = "3.2.17"
    val scalaTestPlusPlay = "6.0.1"

    val macwire = "2.5.9"

    def onlyForScala3[T](scalaVersion: String, scala3Only: T): Seq[T] =
      if (CrossVersion.binaryScalaVersion(scalaVersion) == "3") Seq(scala3Only) else Seq.empty
  }

  object Compile {
    import Versions._

    val grpcStub = "io.grpc" % "grpc-stub" % Versions.grpc

    def akkaActorTyped(sv: String) =
      ("com.typesafe.akka" %% "akka-actor-typed" % Versions.akka(sv)).withConfigurations(Versions.conf(sv))
    def akkaClusterShardingTyped(sv: String) =
      ("com.typesafe.akka" %% "akka-cluster-sharding-typed" % Versions.akka(sv)).withConfigurations(Versions.conf(sv))
    def akkaStream(sv: String) =
      ("com.typesafe.akka" %% "akka-stream" % Versions.akka(sv)).withConfigurations(Versions.conf(sv))
    def akkaDiscovery(sv: String) =
      ("com.typesafe.akka" %% "akka-discovery" % Versions.akka(sv)).withConfigurations(Versions.conf(sv))
    def akkaPersistenceTyped(sv: String) =
      ("com.typesafe.akka" %% "akka-persistence-typed" % Versions.akka(sv)).withConfigurations(Versions.conf(sv))
    def akkaPersistenceQuery(sv: String) =
      ("com.typesafe.akka" %% "akka-persistence-query" % Versions.akka(sv)).withConfigurations(Versions.conf(sv))
    def akkaSerializationJackson(sv: String) =
      ("com.typesafe.akka" %% "akka-serialization-jackson" % Versions.akka(sv)).withConfigurations(Versions.conf(sv))

    def akkaHttp(sv: String) =
      ("com.typesafe.akka" %% "akka-http" % Versions.akkaHttp(sv)).withConfigurations(Versions.conf(sv))
    def akkaHttpSprayJson(sv: String) =
      ("com.typesafe.akka" %% "akka-http-spray-json" % Versions.akkaHttp(sv)).withConfigurations(Versions.conf(sv))
    def akkaHttp2Support(sv: String) =
      ("com.typesafe.akka" %% "akka-http2-support" % Versions.akkaHttp(sv)).withConfigurations(Versions.conf(sv))

    def akkaGrpcCodegen(sv: String) = ("com.lightbend.akka.grpc" %% "akka-grpc-codegen" % Versions.akkaGrpc)
      .withConfigurations(Versions.conf(sv)) // Apache V2 or BSL (depends on USE_BSL sys property)
    def akkaGrpcRuntime(sv: String) = ("com.lightbend.akka.grpc" %% "akka-grpc-runtime" % Versions.akkaGrpc)
      .withConfigurations(Versions.conf(sv)) // Apache V2 or BSL (depends on USE_BSL sys property)

    val play = ("com.typesafe.play" %% "play" % Versions.play)
      .exclude("javax.activation", "javax.activation-api") // Apache V2 (exclusion is "either GPL or CDDL")

    val playJava  = "com.typesafe.play" %% "play-java"  % Versions.play // Apache V2
    val playGuice = "com.typesafe.play" %% "play-guice" % Versions.play // Apache V2
    def playAkkaHttpServer(sv: String) = Seq(
      ("com.typesafe.play" %% "play-akka-http-server" % Versions.play) // Apache V2
        .excludeAll(onlyForScala3(sv, ExclusionRule("com.typesafe.akka", "akka-http-core_2.13")): _*)
    ) ++
      onlyForScala3(
        sv,
        "com.typesafe.akka" %% "akka-http-core" % Versions.akkaHttp(
          sv
        ) % Provided // Apache V2 when Scala 2 or BSL when Scala 3
      )
    def playAkkaHttp2Support(sv: String) = Seq(
      ("com.typesafe.play" %% "play-akka-http2-support" % Versions.play) // Apache V2 when Scala 2 or BSL when Scala 3
        .excludeAll(onlyForScala3(sv, ExclusionRule("com.typesafe.akka", "akka-http-core_2.13")): _*)
        .excludeAll(onlyForScala3(sv, ExclusionRule("com.typesafe.akka", "akka-http2-support_2.13")): _*)
    ) ++
      onlyForScala3(
        sv,
        "com.typesafe.akka" %% "akka-http-core" % Versions.akkaHttp(
          sv
        ) % Provided // Apache V2 when Scala 2 or BSL when Scala 3
      ) ++
      onlyForScala3(
        sv,
        "com.typesafe.akka" %% "akka-http2-support" % Versions.akkaHttp(sv) % Provided
      ) // Apache V2 when Scala 2 or BSL when Scala 3
    val playTest   = "com.typesafe.play" %% "play-test"   % Versions.play // Apache V2
    val playSpecs2 = "com.typesafe.play" %% "play-specs2" % Versions.play // Apache V2

    val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % Versions.scalaTestPlusPlay // Apache V2

    val macwire = "com.softwaremill.macwire" %% "macros" % Versions.macwire % "provided"
  }

  object Test {
    final val Test = sbt.Test

    def akkaActorTestkitTyped(sv: String) = ("com.typesafe.akka" %% "akka-actor-testkit-typed" % Versions.akka(sv))
      .withConfigurations(Versions.conf(sv)) // Apache V2 when Scala 2 or BSL when Scala 3
    def akkaStreamTestkit(sv: String) = ("com.typesafe.akka" %% "akka-stream-testkit" % Versions.akka(sv))
      .withConfigurations(Versions.conf(sv)) // Apache V2 when Scala 2 or BSL when Scala 3

    val junit             = "junit"                   % "junit"       % "4.13.2"           % Test // Common Public License 1.0
    val playAhcWs         = "com.typesafe.play"      %% "play-ahc-ws" % Versions.play      % Test // Apache V2
    val playSpecs2        = Compile.playSpecs2        % Test
    val scalaTest         = "org.scalatest"          %% "scalatest"   % Versions.scalaTest % Test // Apache V2
    val scalaTestPlusPlay = Compile.scalaTestPlusPlay % Test

    val junitInterface = "com.github.sbt" % "junit-interface" % "0.13.3" % "test"
    val logback        = "ch.qos.logback" % "logback-classic" % "1.4.14" % "test"
  }
}
