package build.play.grpc

import sbt._
import sbt.Keys._

object Dependencies {

  object Versions {
    val akka = "2.5.25"

    val akkaGrpc = "0.7.2" // TODO: obtain via sbt-akka-grpc?

    val play  = "2.7.3"
    val lagom = "1.5.4"

    val grpc = "1.24.0" // needs to be in sync with akkaGrpc version?

    val scalaTest         = "3.0.8"
    val scalaTestPlusPlay = "4.0.3"

    val macwire = "2.3.3"
  }

  object Compile {
    val grpcStub = "io.grpc" % "grpc-stub" % Versions.grpc

    val akkaStream = "com.typesafe.akka" %% "akka-stream" % Versions.akka

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

    val junit             = "junit"                   % "junit" % "4.12" % Test // Common Public License 1.0
    val playAhcWs         = "com.typesafe.play"       %% "play-ahc-ws" % Versions.play % Test // Apache V2
    val playSpecs2        = Compile.playSpecs2        % Test
    val scalaTest         = "org.scalatest"           %% "scalatest" % Versions.scalaTest % Test // Apache V2
    val scalaTestPlusPlay = Compile.scalaTestPlusPlay % Test

    val junitInterface = "com.novocode" % "junit-interface" % "0.11" % "test"
  }

}
