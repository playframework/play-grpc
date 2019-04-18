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
crossScalaVersions := Nil // https://github.com/sbt/sbt/issues/3465

val playTestdata = Project("play-grpc-testdata", file("play-testdata"))
  .settings(
    scalacOptions += "-Xlint:-unused,_",  // can't do anything about unused things in generated code
    javacOptions -= "-Xlint:deprecation", // can't do anything about deprecations in generated code
    akkaGrpcExtraGenerators ++= List(
      akka.grpc.gen.javadsl.play.PlayJavaClientCodeGenerator,
      akka.grpc.gen.javadsl.play.PlayJavaServerCodeGenerator(),
      akka.grpc.gen.scaladsl.ScalaMarshallersCodeGenerator,
      akka.grpc.gen.scaladsl.play.PlayScalaClientCodeGenerator,
      akka.grpc.gen.scaladsl.play.PlayScalaServerCodeGenerator(),
    ),
    libraryDependencies ++= Seq(
      Dependencies.Compile.play,
      Dependencies.Compile.grpcStub,
      Dependencies.Compile.playAkkaHttpServer,
      Dependencies.Compile.playAkkaHttp2Support,
    ),
  )
  .pluginTestingSettings
  .enablePlugins(build.play.grpc.NoPublish)

val playTestkit = Project("play-grpc-testkit", file("play-testkit"))
  .dependsOn(playTestdata % "test")
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Compile.play,
      Dependencies.Compile.playTest,
      Dependencies.Test.playAhcWs,
    ),
  )
  .pluginTestingSettings

val playSpecs2 = Project("play-grpc-specs2", file("play-specs2"))
  .dependsOn(playTestkit, playTestkit % "test->test")
  .settings(libraryDependencies += Dependencies.Compile.playSpecs2)
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
  )
  .pluginTestingSettings

val playInteropTestScala = Project("play-grpc-interop-test-scala", file("play-interop-test-scala"))
  .dependsOn(playSpecs2 % Test, playScalaTest % Test)
  .settings(
    akkaGrpcExtraGenerators ++= List(
      akka.grpc.gen.scaladsl.ScalaMarshallersCodeGenerator,
      akka.grpc.gen.scaladsl.play.PlayScalaClientCodeGenerator,
      akka.grpc.gen.scaladsl.play.PlayScalaServerCodeGenerator(),
    ),
    libraryDependencies ++= Seq(
      // TODO https://github.com/akka/akka-grpc/issues/193
      Dependencies.Compile.grpcStub,
      Dependencies.Compile.play,
      Dependencies.Compile.playGuice,
      Dependencies.Compile.playAkkaHttpServer,
      Dependencies.Compile.playAkkaHttp2Support,
      Dependencies.Test.junit,
      Dependencies.Test.playSpecs2,
      Dependencies.Test.scalaTest,
      Dependencies.Test.scalaTestPlusPlay,
    ),
  )
  .pluginTestingSettings
  .enablePlugins(build.play.grpc.NoPublish)

val playInteropTestJava = Project("play-grpc-interop-test-java", file("play-interop-test-java"))
  .dependsOn(playSpecs2 % Test, playScalaTest % Test)
  .settings(
    akkaGrpcExtraGenerators ++= List(
      akka.grpc.gen.javadsl.play.PlayJavaClientCodeGenerator,
      akka.grpc.gen.javadsl.play.PlayJavaServerCodeGenerator(),
    ),
    libraryDependencies ++= Seq(
      // TODO https://github.com/akka/akka-grpc/issues/193
      Dependencies.Compile.grpcStub,
      Dependencies.Compile.play,
      Dependencies.Compile.playGuice,
      Dependencies.Compile.playAkkaHttpServer,
      Dependencies.Compile.playAkkaHttp2Support,
      Dependencies.Compile.playJava,
      Dependencies.Test.junit,
      Dependencies.Test.scalaTest,
    ),
  )
  .enablePlugins(build.play.grpc.NoPublish)
  .pluginTestingSettings

val lagomJavadslGrpcTestKit = Project("lagom-javadsl-grpc-testkit", file("lagom-javadsl-grpc-testkit"))
  .settings(
    libraryDependencies += Dependencies.Compile.lagomJavadslTestKit,
  )
  .pluginTestingSettings

val lagomScaladslGrpcTestKit = Project("lagom-scaladsl-grpc-testkit", file("lagom-scaladsl-grpc-testkit"))
  .settings(
    libraryDependencies += Dependencies.Compile.lagomScaladslTestKit,
  )
  .pluginTestingSettings

val lagomInteropTestScala = Project("lagom-grpc-interop-test-scala", file("lagom-interop-test-scala"))
  .dependsOn(lagomScaladslGrpcTestKit % Test)
  .settings(
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources :=
      Seq(
        AkkaGrpc.Server,
        AkkaGrpc.Client, // the client is only used in tests. See https://github.com/akka/akka-grpc/issues/410
      ),
    akkaGrpcExtraGenerators ++= List(
      akka.grpc.gen.scaladsl.ScalaMarshallersCodeGenerator,
      akka.grpc.gen.scaladsl.play.PlayScalaClientCodeGenerator,
      akka.grpc.gen.scaladsl.play.PlayScalaServerCodeGenerator(),
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
  .dependsOn(lagomJavadslGrpcTestKit % Test)
  .settings(
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Java),
    akkaGrpcGeneratedSources :=
      Seq(
        AkkaGrpc.Server,
        AkkaGrpc.Client, // the client is only used in tests. See https://github.com/akka/akka-grpc/issues/410
      ),
    akkaGrpcExtraGenerators ++= List(
      akka.grpc.gen.javadsl.play.PlayJavaClientCodeGenerator,
      akka.grpc.gen.javadsl.play.PlayJavaServerCodeGenerator(),
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
