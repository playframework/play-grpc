package build.play.grpc

import sbt._
import sbt.Keys._

object Dependencies {

  object Versions {
    val akka = "2.5.21"

    val akkaGrpc = "0.5.0+15-0d4b666d" // TODO: obtain via sbt-akka-grpc?

    val play  = "2.7.0"
    val lagom = "1.5.0-RC2"

    val grpc = "1.16.1" // needs to be in sync with akkaGrpc version?

    val scalaTest         = "3.0.5"
    val scalaTestPlusPlay = "4.0.1"

    val macwire = "2.3.0"
  }

  object Compile {
    val grpcStub = "io.grpc" % "grpc-stub" % Versions.grpc

    val akkaStream = "com.typesafe.akka" %% "akka-stream" % Versions.akka

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

  private val l = libraryDependencies

  val testing = Seq(
    Test.scalaTest,
    Test.junit,
  )

  val playTestdata = l ++= Seq(
    // usually automatically added by `suggestedDependencies`, which doesn't work with ReflectiveCodeGen
    Compile.play,
    Compile.grpcStub,
    Compile.playAkkaHttpServer,
    Compile.playAkkaHttp2Support,
  )

  val playTestkit = l ++= Seq(
    Compile.play,
    Compile.playTest,
    Test.playAhcWs,
  )

  val playSpecs2    = l += Compile.playSpecs2
  val playScalaTest = l += Compile.scalaTestPlusPlay

  val playInteropTestScala = l ++= Seq(
    // TODO https://github.com/akka/akka-grpc/issues/193
    Compile.grpcStub,
    Compile.play,
    Compile.playGuice,
    Compile.playAkkaHttpServer,
    Compile.playAkkaHttp2Support,
    Test.playSpecs2,
    Test.scalaTestPlusPlay,
  ) ++ testing

  val playInteropTestJava = l ++= Seq(
    // TODO https://github.com/akka/akka-grpc/issues/193
    Compile.grpcStub,
    Compile.play,
    Compile.playGuice,
    Compile.playAkkaHttpServer,
    Compile.playAkkaHttp2Support,
    Compile.playJava,
  ) ++ testing

  val lagomJavadslGrpcTestKit = l ++= Seq(
    Compile.lagomJavadslTestKit,
  )

  val lagomScaladslGrpcTestKit = l ++= Seq(
    Compile.lagomScaladslTestKit,
  )

  val lagomInteropTestScala = l ++= Seq(
    // TODO https://github.com/akka/akka-grpc/issues/193
    Compile.grpcStub,
    Compile.lagomScaladslTestKit,
    Compile.playAkkaHttpServer,
    Compile.playAkkaHttp2Support,
    Compile.macwire,
    // Used to force the akka version
    Compile.akkaStream,
  ) ++ testing

  val lagomInteropTestJava = l ++= Seq(
    Test.junitInterface,
    // TODO https://github.com/akka/akka-grpc/issues/193
    Compile.grpcStub,
    Compile.lagomJavadslTestKit,
    Compile.playAkkaHttpServer,
    Compile.playAkkaHttp2Support,
    // Used to force the akka version
    Compile.akkaStream,
  ) ++ testing

}
