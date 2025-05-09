package build.play.grpc

import sbt._
import sbt.Keys._

import org.apache.pekko.grpc.gen.{ BuildInfo => PekkoGrpcBuildInfo }

object Dependencies {

  object Versions {
    val scala212 = "2.12.20"
    val scala213 = "2.13.16"
    val scala3   = "3.3.6"

    // Don't use PekkoGrpcBuildInfo.pekkoHttpVersion or PekkoGrpcBuildInfo.pekkoVersion and prioritize
    // aligning with versions transitively brought in via Play.
    val pekko     = "1.0.3"
    val pekkoHttp = "1.0.1"

    val pekkoGrpc: String = PekkoGrpcBuildInfo.version
    val grpc: String      = PekkoGrpcBuildInfo.grpcVersion

    val play = "3.1.0-M1"

    val scalaTest         = "3.2.19"
    val scalaTestPlusPlay = "8.0.0-M1"

    val macwire = "2.6.5"
  }

  object Compile {
    val grpcStub = "io.grpc" % "grpc-stub" % Versions.grpc

    val pekkoActorTyped           = "org.apache.pekko" %% "pekko-actor-typed"            % Versions.pekko
    val pekkoClusterShardingTyped = "org.apache.pekko" %% "pekko-cluster-sharding-typed" % Versions.pekko
    val pekkoStream               = "org.apache.pekko" %% "pekko-stream"                 % Versions.pekko
    val pekkoDiscovery            = "org.apache.pekko" %% "pekko-discovery"              % Versions.pekko
    val pekkoPersistenceTyped     = "org.apache.pekko" %% "pekko-persistence-typed"      % Versions.pekko
    val pekkoPersistenceQuery     = "org.apache.pekko" %% "pekko-persistence-query"      % Versions.pekko
    val pekkoSerializationJackson = "org.apache.pekko" %% "pekko-serialization-jackson"  % Versions.pekko

    val pekkoHttp          = "org.apache.pekko" %% "pekko-http"            % Versions.pekkoHttp
    val pekkoHttpSprayJson = "org.apache.pekko" %% "pekko-http-spray-json" % Versions.pekkoHttp

    val pekkoGrpcCodegen = "org.apache.pekko" %% "pekko-grpc-codegen" % Versions.pekkoGrpc // Apache V2
    val pekkoGrpcRuntime = "org.apache.pekko" %% "pekko-grpc-runtime" % Versions.pekkoGrpc // Apache V2

    val play = ("org.playframework" %% "play" % Versions.play)
      .exclude("javax.activation", "javax.activation-api") // Apache V2 (exclusion is "either GPL or CDDL")

    val playJava              = "org.playframework" %% "play-java"                % Versions.play // Apache V2
    val playGuice             = "org.playframework" %% "play-guice"               % Versions.play // Apache V2
    val playPekkoHttpServer   = "org.playframework" %% "play-pekko-http-server"   % Versions.play // Apache V2
    val playPekkoHttp2Support = "org.playframework" %% "play-pekko-http2-support" % Versions.play // Apache V2
    val playTest              = "org.playframework" %% "play-test"                % Versions.play // Apache V2
    val playSpecs2            = "org.playframework" %% "play-specs2"              % Versions.play // Apache V2

    val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % Versions.scalaTestPlusPlay // Apache V2

    val macwire = "com.softwaremill.macwire" %% "macros" % Versions.macwire % "provided"
  }

  object Test {
    final val Test = sbt.Test

    val pekkoActorTestkitTyped = "org.apache.pekko" %% "pekko-actor-testkit-typed" % Versions.pekko
    val pekkoStreamTestkit     = "org.apache.pekko" %% "pekko-stream-testkit"      % Versions.pekko

    val junit             = "junit"                   % "junit"       % "4.13.2"           % Test // Common Public License 1.0
    val playAhcWs         = "org.playframework"      %% "play-ahc-ws" % Versions.play      % Test // Apache V2
    val playSpecs2        = Compile.playSpecs2        % Test
    val scalaTest         = "org.scalatest"          %% "scalatest"   % Versions.scalaTest % Test // Apache V2
    val scalaTestPlusPlay = Compile.scalaTestPlusPlay % Test

    val junitInterface = "com.github.sbt" % "junit-interface" % "0.13.3" % "test"
    val logback        = "ch.qos.logback" % "logback-classic" % "1.4.14" % "test"
  }
}
