package build.play.grpc

import java.io.File

import scala.collection.mutable.ListBuffer

import sbt._
import sbt.file
import sbt.internal.inc.classpath.ClasspathUtilities
import sbt.Keys._
import sbt.ProjectRef

import org.apache.pekko.grpc.gen.CodeGenerator
import org.apache.pekko.grpc.gen.CodeGenerator.ScalaBinaryVersion
import org.apache.pekko.grpc.gen.StdoutLogger
import org.apache.pekko.grpc.sbt.GeneratorBridge
import org.apache.pekko.grpc.sbt.PekkoGrpcPlugin
import org.apache.pekko.grpc.sbt.PekkoGrpcPlugin.autoImport._
import protocbridge.{ Artifact => BridgeArtifact }
import protocbridge.Target
import sbtprotoc.ProtocPlugin
import ProtocPlugin.autoImport.PB

/** A plugin that allows to use a code generator compiled in one subproject to be used in a test project */
object ReflectiveCodeGen extends AutoPlugin {
  val generatedLanguages     = SettingKey[Seq[PekkoGrpc.Language]]("reflectiveGrpcGeneratedLanguages")
  val generatedSources       = SettingKey[Seq[PekkoGrpc.GeneratedSource]]("reflectiveGrpcGeneratedSources")
  val extraGenerators        = SettingKey[Seq[String]]("reflectiveGrpcExtraGenerators")
  val codeGeneratorSettings  = settingKey[Seq[String]]("Code generator settings")
  val protocOptions          = settingKey[Seq[String]]("Protoc Options.")
  private val mutableTargets = settingKey[MutableTargets]("Mutable reflective code generator targets")

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
        mutableTargets := new MutableTargets,
        PB.targets     := mutableTargets.value,
        // Put an artifact resolver that returns the project's classpath for our generators
        PB.artifactResolver := Def.taskDyn {
          val cp = classpathFiles(
            (ProjectRef(file("."), "play-grpc-generators") / Compile / fullClasspath).value,
            fileConverter.value,
          )
          val oldResolver = PB.artifactResolver.value
          Def.task { (artifact: BridgeArtifact) =>
            artifact.groupId match {
              case "org.apache.pekko" =>
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
          mutableTargets.value,
          scalaBinaryVersion.value,
          fileConverter.value,
        ),
        PB.recompile ~= (_ => true),
        (Compile / PB.protoSources) := PB.protoSources.value ++ Seq(
          PB.externalIncludePath.value,
          sourceDirectory.value / "proto",
        ),
      ),
    ) ++ Seq(
      (Global / codeGeneratorSettings) := Nil,
      (Global / generatedLanguages)    := Seq(PekkoGrpc.Scala),
      (Global / generatedSources)      := Seq(PekkoGrpc.Client, PekkoGrpc.Server),
      (Global / extraGenerators)       := Seq.empty,
      (Global / protocOptions)         := Seq.empty,
      watchSources ++= (ProjectRef(file("."), "play-grpc-generators") / watchSources).value,
    )

  @transient val setCodeGenerator = taskKey[Unit]("grpc-set-code-generator")

  private def loadAndSetGenerator(
      classpath: Classpath,
      languages0: Seq[PekkoGrpc.Language],
      sources0: Seq[PekkoGrpc.GeneratedSource],
      extraGenerators0: Seq[String],
      targetPath: File,
      generatorSettings: Seq[String],
      targets: MutableTargets,
      scalaBinaryVersion: String,
      converter: xsbti.FileConverter,
  ): Unit = {
    val cp = classpathFiles(classpath, converter)
    // ensure to set right parent classloader, so that protocbridge.ProtocCodeGenerator etc are
    // compatible with what is already accessible from this sbt build
    val loader          = ClasspathUtilities.toLoader(cp, classOf[protocbridge.ProtocCodeGenerator].getClassLoader)
    val binaryVersion   = ScalaBinaryVersion(scalaBinaryVersion)
    val pekkoGenerators = PekkoGrpcPlugin.generatorsFor(sources0, languages0, binaryVersion, StdoutLogger)
    val extraGenerators = extraGenerators0.map { generatorClassName =>
      val module = loader.loadClass(s"$generatorClassName$$").getField("MODULE$").get(null)
      GeneratorBridge.sandboxedGenerator(module.asInstanceOf[CodeGenerator], binaryVersion, StdoutLogger)
    }

    targets.replaceWith(PekkoGrpcPlugin.targetsFor(targetPath, generatorSettings, pekkoGenerators ++ extraGenerators))
  }

  private def classpathFiles(classpath: Classpath, converter: xsbti.FileConverter): Seq[File] =
    classpath.map(_.data).map {
      case file: File                        => file
      case virtualFile: xsbti.VirtualFileRef => converter.toPath(virtualFile).toFile
    }

  private final class MutableTargets extends scala.collection.immutable.AbstractSeq[Target] {
    private val underlying = ListBuffer.empty[Target]

    override def apply(index: Int): Target  = underlying(index)
    override def length: Int                = underlying.length
    override def iterator: Iterator[Target] = underlying.iterator

    def replaceWith(targets: Seq[Target]): Unit = {
      underlying.clear()
      underlying ++= targets
    }
  }

  def generateTaskFromProtocPlugin: Def.Initialize[Task[Seq[File]]] =
    // lookup and return `PB.generate := ...` setting from ProtocPlugin
    ProtocPlugin.projectSettings
      .find(_.key.key == PB.generate.key)
      .get
      .init
      .asInstanceOf[Def.Initialize[Task[Seq[File]]]]
}
