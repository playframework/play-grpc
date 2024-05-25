import build.play.grpc.Dependencies
import build.play.grpc.Dependencies.Versions.scala212
import build.play.grpc.ProjectExtensions.AddPluginTest

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
(ThisBuild / dynverVTagPrefix) := false

ThisBuild / organization := "com.typesafe.play"

ThisBuild / scalacOptions ++= List(
  "-encoding",
  "utf8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Xfuture",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
)

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-java8-compat" % VersionScheme.Always,
)

ThisBuild / javacOptions ++= List(
  "-Xlint:unchecked",
  "-Xlint:deprecation",
)

// Only needed for akka, akka-grpc ,...  snapshots
// See also projects/plugins.sbt
//ThisBuild / resolvers += Resolver.sonatypeRepo("snapshots")

val playGrpc = Project("play-grpc", file("."))
aggregateProjects(
  playRuntime,
  playGenerators,
  playTestdata,
  playInteropTestJava,
  playInteropTestScala,
  playTestkit,
  playSpecs2,
  playScalaTest,
)

enablePlugins(build.play.grpc.NoPublish)
Compile / headerCreate / unmanagedSources := ((baseDirectory.value / "project") ** "*.scala").get
crossScalaVersions                        := Nil // https://github.com/sbt/sbt/issues/3465

val playRuntime = Project("play-grpc-runtime", file("play-runtime"))
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Compile.akkaGrpcRuntime(scalaVersion.value),
      Dependencies.Compile.play,
      Dependencies.Compile.akkaDiscovery(scalaVersion.value),
      Dependencies.Compile.akkaHttp(scalaVersion.value),
      Dependencies.Compile.akkaHttp2Support(scalaVersion.value),
      Dependencies.Compile.akkaHttpSprayJson(scalaVersion.value),
    ) ++ Dependencies.Compile.playAkkaHttpServer(scalaVersion.value),
  )

val playTestdata = Project("play-grpc-testdata", file("play-testdata"))
  .dependsOn(playRuntime)
  .settings(
    scalacOptions += "-Xlint:-unused,_",  // can't do anything about unused things in generated code
    javacOptions -= "-Xlint:deprecation", // can't do anything about deprecations in generated code
    ReflectiveCodeGen.extraGenerators ++= List(
      "akka.grpc.gen.scaladsl.ScalaMarshallersCodeGenerator",
      "play.grpc.gen.javadsl.PlayJavaClientCodeGenerator",
      "play.grpc.gen.javadsl.PlayJavaServerCodeGenerator",
      "play.grpc.gen.scaladsl.PlayScalaClientCodeGenerator",
      "play.grpc.gen.scaladsl.PlayScalaServerCodeGenerator",
    ),
    libraryDependencies ++= Seq(
      Dependencies.Compile.play,
      Dependencies.Compile.grpcStub,
      Dependencies.Compile.akkaDiscovery(scalaVersion.value),
    ) ++ Dependencies.Compile.playAkkaHttpServer(scalaVersion.value) ++
      Dependencies.Compile.playAkkaHttp2Support(scalaVersion.value),
  )
  .enablePlugins(build.play.grpc.NoPublish)
  .pluginTestingSettings

val playActionsTestData = Project("play-grpc-actions-testdata", file("play-actions-testdata"))
  .dependsOn(playRuntime)
  .settings(
    scalacOptions += "-Xlint:-unused,_",  // can't do anything about unused things in generated code
    javacOptions -= "-Xlint:deprecation", // can't do anything about deprecations in generated code
    ReflectiveCodeGen.extraGenerators ++= List(
      "play.grpc.gen.scaladsl.PlayScalaServerCodeGenerator",
    ),
    libraryDependencies ++= Seq(
      Dependencies.Compile.play,
      Dependencies.Compile.grpcStub,
      Dependencies.Compile.akkaDiscovery(scalaVersion.value),
    ) ++ Dependencies.Compile.playAkkaHttpServer(scalaVersion.value) ++
      Dependencies.Compile.playAkkaHttp2Support(scalaVersion.value),
  )
  .enablePlugins(build.play.grpc.NoPublish)
  .pluginTestingSettings

val playGenerators = Project(id = "play-grpc-generators", file("play-generators"))
  .enablePlugins(SbtTwirl, BuildInfoPlugin)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Compile.akkaGrpcCodegen(scalaVersion.value),
      Dependencies.Test.scalaTest,
    ),
    buildInfoKeys ++= Seq[BuildInfoKey](organization, name, version, scalaVersion, sbtVersion),
    buildInfoKeys += "akkaGrpcVersion" → Dependencies.Versions.akkaGrpc,
    buildInfoPackage                  := "play.grpc.gen",
    // Only used in build tools (like sbt), so only 2.12 is needed:
    crossScalaVersions := Seq(scala212),
    scalaVersion       := scala212,
  )
  .enablePlugins(
    // We do not publish this project when publishing Scala 3 artifacts. The Scala 2 and Scala 3 publish workflows run in parallel
    // and if we publish this project in both workflows, it could happen that the Scala 3 workflow writes its artifacts first,
    // with a dependency on the BSL licenced akka-grpc-codegen lib. The other, slower, workflow can not override already published artifacts.
    (if (System.getProperty("USE_BSL", "false") == "true") Seq[Plugins](build.play.grpc.NoPublish) else Seq.empty): _*
  )

val playTestkit = Project("play-grpc-testkit", file("play-testkit"))
  .dependsOn(playRuntime, playTestdata % "test", playActionsTestData % "test")
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Compile.play,
      Dependencies.Compile.playTest,
      Dependencies.Test.playAhcWs,
      Dependencies.Compile.akkaDiscovery(scalaVersion.value),
      Dependencies.Compile.akkaActorTyped(scalaVersion.value),
      Dependencies.Compile.akkaStream(scalaVersion.value),
      Dependencies.Compile.akkaSerializationJackson(scalaVersion.value),
    ),
  )
  .pluginTestingSettings

val playSpecs2 = Project("play-grpc-specs2", file("play-specs2"))
  .dependsOn(playRuntime, playTestkit, playTestkit % "test->test")
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Compile.playSpecs2,
      Dependencies.Compile.akkaDiscovery(scalaVersion.value),
      Dependencies.Compile.akkaActorTyped(scalaVersion.value),
      Dependencies.Compile.akkaStream(scalaVersion.value),
      Dependencies.Compile.akkaSerializationJackson(scalaVersion.value),
    ),
  )
  .pluginTestingSettings

val playScalaTest = Project("play-grpc-scalatest", file("play-scalatest"))
  .dependsOn(playTestkit, playTestkit % "test->test")
  .settings(
    (Compile / headerSources / excludeFilter) := {
      val orig = (Test / headerSources / excludeFilter).value
      // The following files have a different license
      orig || "NewGuiceOneServerPerTest.scala" || "NewServerProvider.scala" || "NewBaseOneServerPerTest.scala"
    },
    libraryDependencies ++= Seq(
      Dependencies.Compile.scalaTestPlusPlay,
      Dependencies.Compile.akkaDiscovery(scalaVersion.value),
      Dependencies.Compile.akkaActorTyped(scalaVersion.value),
      Dependencies.Compile.akkaStream(scalaVersion.value),
      Dependencies.Compile.akkaSerializationJackson(scalaVersion.value),
    ),
  )
  .pluginTestingSettings

val playInteropTestScala = Project("play-grpc-interop-test-scala", file("play-interop-test-scala"))
  .dependsOn(playRuntime, playSpecs2 % Test, playScalaTest % Test)
  .settings(
    ReflectiveCodeGen.extraGenerators ++= List(
      "akka.grpc.gen.scaladsl.ScalaMarshallersCodeGenerator",
      "play.grpc.gen.scaladsl.PlayScalaClientCodeGenerator",
      "play.grpc.gen.scaladsl.PlayScalaServerCodeGenerator",
    ),
    libraryDependencies ++= Seq(
      // TODO https://github.com/akka/akka-grpc/issues/193
      Dependencies.Compile.grpcStub,
      Dependencies.Compile.play,
      Dependencies.Compile.playGuice,
      Dependencies.Compile.akkaDiscovery(scalaVersion.value),
      Dependencies.Compile.akkaActorTyped(scalaVersion.value),
      Dependencies.Compile.akkaSerializationJackson(scalaVersion.value),
      Dependencies.Test.junit,
      Dependencies.Test.playSpecs2,
      Dependencies.Test.scalaTest,
      Dependencies.Test.scalaTestPlusPlay,
    ) ++ Dependencies.Compile.playAkkaHttpServer(scalaVersion.value) ++
      Dependencies.Compile.playAkkaHttp2Support(scalaVersion.value),
  )
  .pluginTestingSettings
  .enablePlugins(build.play.grpc.NoPublish)

val playInteropTestJava = Project("play-grpc-interop-test-java", file("play-interop-test-java"))
  .dependsOn(playRuntime, playSpecs2 % Test, playScalaTest % Test)
  .settings(
    ReflectiveCodeGen.extraGenerators ++= List(
      "play.grpc.gen.javadsl.PlayJavaClientCodeGenerator",
      "play.grpc.gen.javadsl.PlayJavaServerCodeGenerator",
    ),
    libraryDependencies ++= Seq(
      // TODO https://github.com/akka/akka-grpc/issues/193
      Dependencies.Compile.grpcStub,
      Dependencies.Compile.play,
      Dependencies.Compile.playGuice,
      Dependencies.Compile.playJava,
      Dependencies.Compile.akkaDiscovery(scalaVersion.value),
      Dependencies.Compile.akkaActorTyped(scalaVersion.value),
      Dependencies.Compile.akkaSerializationJackson(scalaVersion.value),
      Dependencies.Test.junit,
      Dependencies.Test.scalaTest,
    ) ++ Dependencies.Compile.playAkkaHttpServer(scalaVersion.value) ++
      Dependencies.Compile.playAkkaHttp2Support(scalaVersion.value),
  )
  .enablePlugins(build.play.grpc.NoPublish)
  .pluginTestingSettings

Global / cancelable := true
