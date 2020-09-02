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
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
)

ThisBuild / javacOptions ++= List(
  "-Xlint:unchecked",
  "-Xlint:deprecation",
)

// Only needed for akka, akka-grpc ,...  snapshots
// See also projects/plugins.sbt
ThisBuild / resolvers += Resolver.bintrayRepo("akka", "maven")

val playGrpc = Project("play-grpc", file("."))
aggregateProjects(
  playGenerators,
  playInteropTestJava,
  playInteropTestScala,
  playRuntime,
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
crossScalaVersions := Nil // https://github.com/sbt/sbt/issues/3465

val playRuntime = Project("play-grpc-runtime", file("play-runtime"))
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Compile.akkaGrpcRuntime,
      Dependencies.Compile.play,
      Dependencies.Compile.playAkkaHttpServer,
      Dependencies.Compile.akkaDiscovery,
    ),
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
      Dependencies.Compile.playAkkaHttpServer,
      Dependencies.Compile.playAkkaHttp2Support,
      Dependencies.Compile.akkaDiscovery,
    ),
  )
  .pluginTestingSettings
  .enablePlugins(build.play.grpc.NoPublish)

val playActionsTestData = Project("play-grpc-actions-testdata", file("play-actions-testdata"))
  .dependsOn(playRuntime)
  .settings(
    scalacOptions += "-Xlint:-unused,_",  // can't do anything about unused things in generated code
    javacOptions -= "-Xlint:deprecation", // can't do anything about deprecations in generated code
    ReflectiveCodeGen.extraGenerators ++= List(
      "play.grpc.gen.scaladsl.PlayScalaServerCodeGenerator",
    ),
    ReflectiveCodeGen.codeGeneratorSettings += "use_play_actions",
    libraryDependencies ++= Seq(
      Dependencies.Compile.play,
      Dependencies.Compile.grpcStub,
      Dependencies.Compile.playAkkaHttpServer,
      Dependencies.Compile.playAkkaHttp2Support,
      Dependencies.Compile.akkaDiscovery,
    ),
  )
  .pluginTestingSettings
  .enablePlugins(build.play.grpc.NoPublish)

val playGenerators = Project("play-grpc-generators", file("play-generators"))
  .enablePlugins(SbtTwirl, BuildInfoPlugin)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Compile.akkaGrpcCodegen,
      Dependencies.Test.scalaTest,
    ),
  )

val playTestkit = Project("play-grpc-testkit", file("play-testkit"))
  .dependsOn(playTestdata % "test")
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Compile.play,
      Dependencies.Compile.playTest,
      Dependencies.Test.playAhcWs,
      Dependencies.Compile.akkaDiscovery,
    ),
  )
  .pluginTestingSettings

val playSpecs2 = Project("play-grpc-specs2", file("play-specs2"))
  .dependsOn(playTestkit, playTestkit % "test->test")
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Compile.playSpecs2,
      Dependencies.Compile.akkaDiscovery,
    ),
  )
  .pluginTestingSettings

val playScalaTest = Project("play-grpc-scalatest", file("play-scalatest"))
  .dependsOn(playTestkit, playTestkit % "test->test")
  .settings(
    excludeFilter in (Compile, headerSources) := {
      val orig = (excludeFilter in (Test, headerSources)).value
      // The following files have a different license
      orig || "NewGuiceOneServerPerTest.scala" || "NewServerProvider.scala" || "NewBaseOneServerPerTest.scala"
    },
    libraryDependencies += Dependencies.Compile.scalaTestPlusPlay,
    libraryDependencies += Dependencies.Compile.akkaDiscovery,
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
      Dependencies.Compile.playAkkaHttpServer,
      Dependencies.Compile.playAkkaHttp2Support,
      Dependencies.Compile.akkaDiscovery,
      Dependencies.Test.junit,
      Dependencies.Test.playSpecs2,
      Dependencies.Test.scalaTest,
      Dependencies.Test.scalaTestPlusPlay,
    ),
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
      Dependencies.Compile.playAkkaHttpServer,
      Dependencies.Compile.playAkkaHttp2Support,
      Dependencies.Compile.playJava,
      Dependencies.Compile.akkaDiscovery,
      Dependencies.Test.junit,
      Dependencies.Test.scalaTest,
    ),
  )
  .enablePlugins(build.play.grpc.NoPublish)
  .pluginTestingSettings

val lagomJavadslGrpcTestKit = Project("lagom-javadsl-grpc-testkit", file("lagom-javadsl-grpc-testkit"))
  .settings(
    libraryDependencies += Dependencies.Compile.lagomJavadslTestKit,
    libraryDependencies += Dependencies.Compile.akkaDiscovery,
  )
  .pluginTestingSettings

val lagomScaladslGrpcTestKit = Project("lagom-scaladsl-grpc-testkit", file("lagom-scaladsl-grpc-testkit"))
  .settings(
    libraryDependencies += Dependencies.Compile.lagomScaladslTestKit,
    libraryDependencies += Dependencies.Compile.akkaDiscovery,
  )
  .pluginTestingSettings

val lagomInteropTestScala = Project("lagom-grpc-interop-test-scala", file("lagom-interop-test-scala"))
  .dependsOn(playRuntime, lagomScaladslGrpcTestKit % Test)
  .settings(
    ReflectiveCodeGen.generatedLanguages := Seq(AkkaGrpc.Scala),
    ReflectiveCodeGen.extraGenerators ++= List(
      "akka.grpc.gen.scaladsl.ScalaMarshallersCodeGenerator",
      "play.grpc.gen.scaladsl.PlayScalaClientCodeGenerator",
      "play.grpc.gen.scaladsl.PlayScalaServerCodeGenerator",
    ),
    libraryDependencies ++= Seq(
      // TODO https://github.com/akka/akka-grpc/issues/193
      Dependencies.Compile.grpcStub,
      Dependencies.Compile.lagomScaladslTestKit,
      Dependencies.Compile.playAkkaHttpServer,
      Dependencies.Compile.playAkkaHttp2Support,
      Dependencies.Compile.macwire,
      // Used to force the akka version
      Dependencies.Compile.akkaStream,
      Dependencies.Test.junit,
      Dependencies.Test.scalaTest,
    ),
  )
  .pluginTestingSettings
  .enablePlugins(build.play.grpc.NoPublish)

val lagomInteropTestJava = Project("lagom-grpc-interop-test-java", file("lagom-interop-test-java"))
  .dependsOn(playRuntime, lagomJavadslGrpcTestKit % Test)
  .settings(
    ReflectiveCodeGen.generatedLanguages := Seq(AkkaGrpc.Java),
    ReflectiveCodeGen.extraGenerators ++= List(
      "play.grpc.gen.javadsl.PlayJavaClientCodeGenerator",
      "play.grpc.gen.javadsl.PlayJavaServerCodeGenerator",
    ),
    libraryDependencies ++= Seq(
      // TODO https://github.com/akka/akka-grpc/issues/193
      Dependencies.Compile.grpcStub,
      Dependencies.Compile.lagomJavadslTestKit,
      Dependencies.Compile.playAkkaHttpServer,
      Dependencies.Compile.playAkkaHttp2Support,
      // Used to force the akka version
      Dependencies.Compile.akkaStream,
      Dependencies.Test.junit,
      Dependencies.Test.junitInterface,
      Dependencies.Test.scalaTest,
    ),
  )
  .pluginTestingSettings
  .enablePlugins(build.play.grpc.NoPublish)

val docs = Project("play-grpc-docs", file("docs"))
  .enablePlugins(AkkaParadoxPlugin)
  .settings(
    // Make sure code generation is run before paradox:
    (Compile / paradox) := (Compile / paradox).dependsOn(Compile / compile).value,
    paradoxGroups := Map(
      "Language"  -> Seq("Scala", "Java"),
      "Buildtool" -> Seq("sbt", "Gradle", "Maven"),
    ),
    paradoxProperties ++= Map(
      "grpc.version"      -> Dependencies.Versions.grpc,
      "akka.grpc.version" -> Dependencies.Versions.akkaGrpc,
    ),
    resolvers += Resolver.jcenterRepo,
  )
  .enablePlugins(build.play.grpc.NoPublish)

cancelable in Global := true
