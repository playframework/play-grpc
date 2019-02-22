import build.play.grpc.Dependencies
import build.play.grpc.ProjectExtensions.AddPluginTest

ThisBuild / organization := "com.lightbend.play"

ThisBuild / scalacOptions ++= List(
  "-encoding",
  "utf8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
)

ThisBuild / javacOptions ++= List(
  "-Xlint:unchecked",
  "-Xlint:deprecation",
)

val playGrpc = Project("play-grpc", file("."))
aggregateProjects(
  playInteropTestJava,
  playInteropTestScala,
  playTestkit,
  playSpecs2,
  playScalaTest,
  playTestdata,
  lagomScaladslGrpcTestKit,
  lagomJavadslGrpcTestKit,
  lagomInteropTestJava,
  lagomInteropTestScala,
  docs,
)

enablePlugins(build.play.grpc.NoPublish)
unmanagedSources in (Compile, headerCreate) := ((baseDirectory.value / "project") ** "*.scala").get

lazy val playTestdata = Project(
  id = "play-grpc-testdata",
  base = file("play-testdata"),
).settings(Dependencies.playTestdata)
  .settings(
    scalacOptions += "-Xlint:-unused,_",  // can't do anything about unused things in generated code
    javacOptions -= "-Xlint:deprecation", // can't do anything about deprecations in generated code
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
  id = "play-grpc-testkit",
  base = file("play-testkit"),
).dependsOn(playTestdata % "test")
  .settings(Dependencies.playTestkit)
  .pluginTestingSettings

val playSpecs2 = Project("play-grpc-specs2", file("play-specs2"))
  .dependsOn(playTestkit, playTestkit % "test->test")
  .settings(
    Dependencies.playSpecs2,
  )
  .pluginTestingSettings

val playScalaTest = Project("play-grpc-scalatest", file("play-scalatest"))
  .dependsOn(playTestkit, playTestkit % "test->test")
  .settings(
    Dependencies.playScalaTest,
    excludeFilter in (Compile, headerSources) := {
      val orig = (excludeFilter in (Test, headerSources)).value
      // The following files have a different license
      orig || "NewGuiceOneServerPerTest.scala" || "NewServerProvider.scala" || "NewBaseOneServerPerTest.scala"
    },
  )
  .pluginTestingSettings

lazy val playInteropTestScala = Project(
  id = "play-grpc-interop-test-scala",
  base = file("play-interop-test-scala"),
).dependsOn(playSpecs2 % Test, playScalaTest % Test)
  .settings(Dependencies.playInteropTestScala)
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
  id = "play-grpc-interop-test-java",
  base = file("play-interop-test-java"),
).dependsOn(playSpecs2 % Test, playScalaTest % Test)
  .settings(Dependencies.playInteropTestJava)
  .settings(
    akkaGrpcExtraGenerators ++= List(
      akka.grpc.gen.javadsl.play.PlayJavaClientCodeGenerator,
      akka.grpc.gen.javadsl.play.PlayJavaServerCodeGenerator,
    ),
  )
  .enablePlugins(build.play.grpc.NoPublish)
  .pluginTestingSettings

lazy val lagomJavadslGrpcTestKit = Project(
  id = "lagom-javadsl-grpc-testkit",
  base = file("lagom-javadsl-grpc-testkit"),
)
  .settings(Dependencies.lagomJavadslGrpcTestKit)
  .pluginTestingSettings

lazy val lagomScaladslGrpcTestKit = Project(
  id = "lagom-scaladsl-grpc-testkit",
  base = file("lagom-scaladsl-grpc-testkit"),
)
  .settings(Dependencies.lagomScaladslGrpcTestKit)
  .pluginTestingSettings

lazy val lagomInteropTestScala = Project(
  id = "lagom-grpc-interop-test-scala",
  base = file("lagom-interop-test-scala"),
).dependsOn(lagomScaladslGrpcTestKit % Test)
  .settings(Dependencies.lagomInteropTestScala)
  .settings(
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources :=
      Seq(
        AkkaGrpc.Server,
        AkkaGrpc.Client // the client is only used in tests. See https://github.com/akka/akka-grpc/issues/410
      ),
    akkaGrpcExtraGenerators ++= List(
      akka.grpc.gen.scaladsl.ScalaMarshallersCodeGenerator,
      akka.grpc.gen.scaladsl.play.PlayScalaClientCodeGenerator,
      akka.grpc.gen.scaladsl.play.PlayScalaServerCodeGenerator,
    ),
  )
  .enablePlugins(build.play.grpc.NoPublish)
  .pluginTestingSettings

lazy val lagomInteropTestJava = Project(
  id = "lagom-grpc-interop-test-java",
  base = file("lagom-interop-test-java"),
).dependsOn(lagomJavadslGrpcTestKit % Test)
  .settings(Dependencies.lagomInteropTestJava)
  .settings(
    akkaGrpcExtraGenerators ++= List(
      akka.grpc.gen.javadsl.play.PlayJavaClientCodeGenerator,
      akka.grpc.gen.javadsl.play.PlayJavaServerCodeGenerator,
    ),
  )
  .enablePlugins(build.play.grpc.NoPublish)
  .pluginTestingSettings

lazy val docs = Project(
  id = "play-grpc-docs",
  base = file("docs"),
)
// Make sure code generation is ran:
  .enablePlugins(AkkaParadoxPlugin)
  .enablePlugins(build.play.grpc.NoPublish)
  .settings(
    // Make sure code generation is ran before paradox:
    (Compile / paradox) := (Compile / paradox).dependsOn(Compile / compile).value,
    paradoxGroups := Map(
      "Language"  -> Seq("Scala", "Java"),
      "Buildtool" -> Seq("sbt", "Gradle", "Maven"),
    ),
    paradoxProperties ++= Map(
      "grpc.version" → Dependencies.Versions.grpc,
    ),
    resolvers += Resolver.jcenterRepo,
  )

cancelable in Global := true
