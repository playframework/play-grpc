import build.play.grpc.Dependencies
import build.play.grpc.Dependencies.Versions.{ scala211, scala212 }
import build.play.grpc.ProjectExtensions._

ThisBuild / organization := "com.lightbend.play"

ThisBuild / scalaVersion := scala212
ThisBuild / crossScalaVersions := Seq(scala211, scala212)

ThisBuild / scalacOptions ++= List(
  "-encoding", "utf8",
  "-deprecation",
  "-feature",
  "-unchecked",
//"-Xlint",
//"-Xfuture",
//"-Yno-adapted-args",
//"-Ywarn-dead-code",
//"-Ywarn-numeric-widen",
//"-Ywarn-value-discard",
)

ThisBuild / javacOptions ++= List(
  "-Xlint:unchecked",
  "-Xlint:deprecation",
)

val commonSettings = build.play.grpc.Formatting.formatSettings

lazy val playTestdata = Project(
    id="play-grpc-testdata",
    base=file("play-testdata")
  )
  .settings(Dependencies.playTestdata)
  .settings(commonSettings)
  .settings(
    akkaGrpcExtraGenerators ++= List(
      akka.grpc.gen.javadsl.play.PlayJavaClientCodeGenerator,
      akka.grpc.gen.javadsl.play.PlayJavaServerCodeGenerator,
      akka.grpc.gen.scaladsl.ScalaMarshallersCodeGenerator,
      akka.grpc.gen.scaladsl.play.PlayScalaClientCodeGenerator,
      akka.grpc.gen.scaladsl.play.PlayScalaServerCodeGenerator,
    ),
  )
  .enablePlugins(build.play.grpc.NoPublish)
  .pluginTestingSettings

lazy val playTestkit = Project(
    id="play-grpc-testkit",
    base = file("play-testkit")
  )
  .dependsOn(playTestdata % "test")
  .settings(Dependencies.playTestkit)
  .settings(commonSettings)
  .pluginTestingSettings

val playSpecs2 = Project("play-grpc-specs2", file("play-specs2"))
  .dependsOn(playTestkit, playTestkit % "test->test")
  .settings(
    commonSettings,
    Dependencies.playSpecs2,
  )
  .pluginTestingSettings

val playScalaTest = Project("play-grpc-scalatest", file("play-scalatest"))
  .dependsOn(playTestkit, playTestkit % "test->test")
  .settings(
    commonSettings,
    Dependencies.playScalaTest,
    excludeFilter in (Compile, headerSources) := {
      val orig = (excludeFilter in (Test, headerSources)).value
      // The following files have a different license
      orig || "NewGuiceOneServerPerTest.scala" || "NewServerProvider.scala" || "NewBaseOneServerPerTest.scala"
    },
  )
  .pluginTestingSettings

lazy val playInteropTestScala = Project(
    id="play-grpc-interop-test-scala",
    base = file("play-interop-test-scala")
  )
  .dependsOn(playSpecs2 % Test, playScalaTest % Test)
  .settings(Dependencies.playInteropTestScala)
  .settings(commonSettings)
  .settings(
    akkaGrpcExtraGenerators ++= List(
      akka.grpc.gen.scaladsl.ScalaMarshallersCodeGenerator,
      akka.grpc.gen.scaladsl.play.PlayScalaClientCodeGenerator,
      akka.grpc.gen.scaladsl.play.PlayScalaServerCodeGenerator,
    ),
  )
  .enablePlugins(build.play.grpc.NoPublish)
  .pluginTestingSettings

lazy val playInteropTestJava = Project(
    id="play-grpc-interop-test-java",
    base = file("play-interop-test-java")
  )
  .dependsOn(playSpecs2 % Test, playScalaTest % Test)
  .settings(Dependencies.playInteropTestJava)
  .settings(commonSettings)
  .settings(
    akkaGrpcExtraGenerators ++= List(
      akka.grpc.gen.javadsl.play.PlayJavaClientCodeGenerator,
      akka.grpc.gen.javadsl.play.PlayJavaServerCodeGenerator,
    ),
  )
  .enablePlugins(build.play.grpc.NoPublish)
  .pluginTestingSettings

lazy val root = Project(
    id = "play-grpc",
    base = file(".")
  )
  .aggregate(
    playInteropTestJava,
    playInteropTestScala,
    playTestkit,
    playSpecs2,
    playScalaTest,
    playTestdata,
  )
  .enablePlugins(build.play.grpc.NoPublish)
  .settings(
    unmanagedSources in (Compile, headerCreate) := (baseDirectory.value / "project").**("*.scala").get
  )

cancelable in Global := true
