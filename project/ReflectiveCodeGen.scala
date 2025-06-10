package build.play.grpc

import java.io.File

import scala.collection.mutable.ListBuffer

import sbt._
import sbt.file
import sbt.internal.inc.classpath.ClasspathUtilities
import sbt.Keys._
import sbt.ProjectRef

import akka.grpc.sbt.AkkaGrpcPlugin.autoImport._
import protocbridge.{ Artifact => BridgeArtifact }
import protocbridge.Target
import sbtprotoc.ProtocPlugin
import ProtocPlugin.autoImport.PB

/** A plugin that allows to use a code generator compiled in one subproject to be used in a test project */
object ReflectiveCodeGen extends AutoPlugin {
  val generatedLanguages    = SettingKey[Seq[AkkaGrpc.Language]]("reflectiveGrpcGeneratedLanguages")
  val generatedSources      = SettingKey[Seq[AkkaGrpc.GeneratedSource]]("reflectiveGrpcGeneratedSources")
  val extraGenerators       = SettingKey[Seq[String]]("reflectiveGrpcExtraGenerators")
  val codeGeneratorSettings = settingKey[Seq[String]]("Code generator settings")
  val protocOptions         = settingKey[Seq[String]]("Protoc Options.")

  // needed to be able to override the PB.generate task reliably
  override def requires = ProtocPlugin

  override def projectSettings: Seq[Def.Setting[_]] =
    inConfig(Compile)(
      Seq(
        PB.protocOptions := protocOptions.value,
        PB.generate      :=
          // almost the same as `Def.sequential` but will return the "middle" value, ie. the result of the generation
          // Defines three steps:
          //   1) dynamically load the current code generator and plug it in the mutable generator
          //   2) run the generator
          //   3) delete the generation cache because it doesn't know that the generator may change
          Def.taskDyn {
            val _ = setCodeGenerator.value
            Def.taskDyn {
              val generationResult = generateTaskFromProtocPlugin.value

              Def.task {
                // path is defined in ProtocPlugin.sourceGeneratorTask
                val file = (PB.generate / streams).value.cacheDirectory / s"protobuf_${scalaBinaryVersion.value}"
                IO.delete(file)

                generationResult
              }
            }
          }.value,
        // HACK: make the targets mutable, so we can fill them while running the above PB.generate
        PB.targets := scala.collection.mutable.ListBuffer.empty,
        // Put an artifact resolver that returns the project's classpath for our generators
        PB.artifactResolver := Def.taskDyn {
          val cp          = (ProjectRef(file("."), "play-grpc-generators") / Compile / fullClasspath).value.map(_.data)
          val oldResolver = PB.artifactResolver.value
          Def.task { (artifact: BridgeArtifact) =>
            artifact.groupId match {
              case "com.lightbend.akka.grpc" =>
                cp
              case _ =>
                oldResolver(artifact)
            }
          }
        }.value,
        setCodeGenerator := loadAndSetGenerator(
          // the magic sauce: use the output classpath from the the sbt-plugin project and instantiate generators from there
          (ProjectRef(file("."), "play-grpc-generators") / Compile / fullClasspath).value,
          generatedLanguages.value,
          generatedSources.value,
          extraGenerators.value,
          sourceManaged.value,
          codeGeneratorSettings.value,
          PB.targets.value.asInstanceOf[ListBuffer[Target]],
          scalaBinaryVersion.value,
        ),
        PB.recompile ~= (_ => true),
        (Compile / PB.protoSources) := PB.protoSources.value ++ Seq(
          PB.externalIncludePath.value,
          sourceDirectory.value / "proto",
        ),
      ),
    ) ++ Seq(
      (Global / codeGeneratorSettings) := Nil,
      (Global / generatedLanguages)    := Seq(AkkaGrpc.Scala),
      (Global / generatedSources)      := Seq(AkkaGrpc.Client, AkkaGrpc.Server),
      (Global / extraGenerators)       := Seq.empty,
      (Global / protocOptions)         := Seq.empty,
      watchSources ++= (ProjectRef(file("."), "play-grpc-generators") / watchSources).value,
    )

  val setCodeGenerator = taskKey[Unit]("grpc-set-code-generator")

  def loadAndSetGenerator(
      classpath: Classpath,
      languages0: Seq[AkkaGrpc.Language],
      sources0: Seq[AkkaGrpc.GeneratedSource],
      extraGenerators0: Seq[String],
      targetPath: File,
      generatorSettings: Seq[String],
      targets: ListBuffer[Target],
      scalaBinaryVersion: String,
  ): Unit = {
    val languages = languages0.mkString(", ")
    val sources   = sources0.mkString(", ")

    val cp = classpath.map(_.data)
    // ensure to set right parent classloader, so that protocbridge.ProtocCodeGenerator etc are
    // compatible with what is already accessible from this sbt build
    val loader = ClasspathUtilities.toLoader(cp, classOf[protocbridge.ProtocCodeGenerator].getClassLoader)
    import scala.reflect.runtime.universe
    import scala.tools.reflect.ToolBox

    // NOTE to maintainers:
    //  - For some reason, the reflective code below fails compilation when trying to run it with
    //    with more than one extraGenerators0 at a time. For that reason I've split the generated code and
    //    recreate it over and over for each generator. Performance-wise it has a negligible impact.
    //    (see also https://github.com/playframework/play-grpc/pull/356#issuecomment-832092996)
    val tb         = universe.runtimeMirror(loader).mkToolBox()
    val akkaSource =
      s"""import akka.grpc.sbt.AkkaGrpcPlugin
         |import akka.grpc.sbt.GeneratorBridge
         |import AkkaGrpcPlugin.autoImport._
         |import AkkaGrpc._
         |import akka.grpc.gen.CodeGenerator.ScalaBinaryVersion
         |
         |val languages: Seq[AkkaGrpc.Language] = Seq($languages)
         |val sources: Seq[AkkaGrpc.GeneratedSource] = Seq($sources)
         |val scalaBinaryVersion = ScalaBinaryVersion("$scalaBinaryVersion")
         |
         |val logger = akka.grpc.gen.StdoutLogger
         |
         |(targetPath: java.io.File, settings: Seq[String]) => {
         |  val generators = AkkaGrpcPlugin.generatorsFor(sources, languages, scalaBinaryVersion, logger)
         |  AkkaGrpcPlugin.targetsFor(targetPath, settings, generators)
         |}
        """.stripMargin
    val akkaGeneratorsF = tb.eval(tb.parse(akkaSource)).asInstanceOf[(File, Seq[String]) => Seq[Target]]
    val akkaGenerators  = akkaGeneratorsF(targetPath, generatorSettings)

    def source(singleGenerator: String) =
      s"""import akka.grpc.sbt.AkkaGrpcPlugin
         |import akka.grpc.sbt.GeneratorBridge
         |import AkkaGrpcPlugin.autoImport._
         |import AkkaGrpc._
         |import akka.grpc.gen.CodeGenerator.ScalaBinaryVersion
         |
         |val scalaBinaryVersion = ScalaBinaryVersion("$scalaBinaryVersion")
         |
         |val logger = akka.grpc.gen.StdoutLogger
         |
         |(targetPath: java.io.File, settings: Seq[String]) => {
         |  val generators = Seq(GeneratorBridge.sandboxedGenerator($singleGenerator, scalaBinaryVersion, akka.grpc.gen.StdoutLogger))
         |  AkkaGrpcPlugin.targetsFor(targetPath, settings, generators)
         |}
        """.stripMargin
    val extras = extraGenerators0.flatMap { singleGenerator =>
      val generatorsF = tb.eval(tb.parse(source(singleGenerator))).asInstanceOf[(File, Seq[String]) => Seq[Target]]
      generatorsF(targetPath, generatorSettings)
    }

    targets.clear()
    targets ++= akkaGenerators
    targets ++= extras
  }

  def generateTaskFromProtocPlugin: Def.Initialize[Task[Seq[File]]] =
    // lookup and return `PB.generate := ...` setting from ProtocPlugin
    ProtocPlugin.projectSettings
      .find(_.key.key == PB.generate.key)
      .get
      .init
      .asInstanceOf[Def.Initialize[Task[Seq[File]]]]
}
