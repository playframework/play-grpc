package build.play.grpc

import sbt._
import sbt.Keys._

object Dependencies {

  object Versions {
    val scala212 = "2.12.11"
    val scala213 = "2.13.1"

    val akka = "2.6.5"

    // This version must be in sync with the version of "sbt-akka-grpc" in "project/plugins.sbt"
    val akkaGrpc = "0.8.4" // TODO: obtain via sbt-akka-grpc?

    val play  = "2.8.1"
    val lagom = "1.6.2"

    val grpc = "1.29.0" // needs to be in sync with akkaGrpc version?

    val scalaTest         = "3.1.1"
    val scalaTestPlusPlay = "5.1.0"

    val macwire = "2.3.5"
  }

  object Compile {
    val grpcStub = "io.grpc" % "grpc-stub" % Versions.grpc

    val akkaActorTyped           = "com.typesafe.akka" %% "akka-actor-typed"            % Versions.akka
    val akkaClusterShardingTyped = "com.typesafe.akka" %% "akka-cluster-sharding-typed" % Versions.akka
    val akkaStream               = "com.typesafe.akka" %% "akka-stream"                 % Versions.akka
    val akkaDiscovery            = "com.typesafe.akka" %% "akka-discovery"              % Versions.akka
    val akkaPersistenceTyped     = "com.typesafe.akka" %% "akka-persistence-typed"      % Versions.akka
    val akkaPersistenceQuery     = "com.typesafe.akka" %% "akka-persistence-query"      % Versions.akka

    val akkaGrpcCodegen = "com.lightbend.akka.grpc" %% "akka-grpc-codegen" % Versions.akkaGrpc // Apache V2
    val akkaGrpcRuntime = "com.lightbend.akka.grpc" %% "akka-grpc-runtime" % Versions.akkaGrpc // Apache V2

    val play = ("com.typesafe.play" %% "play" % Versions.play).exclude("javax.activation", "javax.activation-api") // Apache V2 (exclusion is "either GPL or CDDL")

    val playJava             = "com.typesafe.play" %% "play-java"               % Versions.play // Apache V2
    val playGuice            = "com.typesafe.play" %% "play-guice"              % Versions.play // Apache V2
    val playAkkaHttpServer   = "com.typesafe.play" %% "play-akka-http-server"   % Versions.play // Apache V2
    val playAkkaHttp2Support = "com.typesafe.play" %% "play-akka-http2-support" % Versions.play // Apache V2
    val playTest             = "com.typesafe.play" %% "play-test"               % Versions.play // Apache V2
    val playSpecs2           = "com.typesafe.play" %% "play-specs2"             % Versions.play // Apache V2

    val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % Versions.scalaTestPlusPlay // Apache V2

    val lagomJavadslTestKit  = "com.lightbend.lagom" %% "lagom-javadsl-testkit"  % Versions.lagom
    val lagomScaladslTestKit = "com.lightbend.lagom" %% "lagom-scaladsl-testkit" % Versions.lagom

    val macwire = "com.softwaremill.macwire" %% "macros" % Versions.macwire % "provided"
  }

  object Test {
    final val Test = sbt.Test

    val akkaActorTestkitTyped = "com.typesafe.akka" %% "akka-actor-testkit-typed" % Versions.akka
    val akkaStreamTestkit     = "com.typesafe.akka" %% "akka-stream-testkit"      % Versions.akka

    val junit             = "junit"                   % "junit" % "4.13" % Test // Common Public License 1.0
    val playAhcWs         = "com.typesafe.play"       %% "play-ahc-ws" % Versions.play % Test // Apache V2
    val playSpecs2        = Compile.playSpecs2        % Test
    val scalaTest         = "org.scalatest"           %% "scalatest" % Versions.scalaTest % Test // Apache V2
    val scalaTestPlusPlay = Compile.scalaTestPlusPlay % Test

    val junitInterface = "com.novocode" % "junit-interface" % "0.11" % "test"
  }

}
