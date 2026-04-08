package build.play.grpc

import sbt._
import sbt.Keys._

object WorkaroundTwirlFormatCompat {
  private val TemplateSources = Seq(
    "templates/JavaClient/txt/GenMethodImports.template.scala",
    "templates/JavaClient/txt/ClientPowerApi.template.scala",
    "templates/JavaClient/txt/Client.template.scala",
    "templates/ScalaServer/txt/Handler.template.scala",
    "templates/ScalaServer/txt/PowerApiTrait.template.scala",
    "templates/ScalaClient/txt/Client.template.scala",
    "templates/JavaServer/txt/PowerApiInterface.template.scala",
    "templates/JavaServer/txt/Handler.template.scala",
    "templates/JavaCommon/txt/ApiInterface.template.scala",
    "templates/ScalaCommon/txt/ApiTrait.template.scala",
    "templates/ScalaCommon/txt/Marshallers.template.scala",
  )

  private val CompatTraitSource =
    """package templates
      |
      |trait TwirlFormatCompat[T <: play.twirl.api.Appendable[T], F <: play.twirl.api.Format[T]] {
      |  self: play.twirl.api.BaseScalaTemplate[T, F] =>
      |
      |  final def format: F = this.$twirl__format
      |}
      |""".stripMargin

  private val CompatMixin =
    " with _root_.templates.TwirlFormatCompat[play.twirl.api.TxtFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.TxtFormat.Appendable]] with _root_.play.twirl.api.Template"

  def generate: Def.Initialize[Task[Seq[File]]] = Def.task {
    val outputDir  = (Compile / sourceManaged).value / "pekko-grpc-twirl-compat"
    val extractDir = streams.value.cacheDirectory / "pekko-grpc-twirl-compat"

    val sourceJar = (Compile / updateClassifiers).value.allFiles
      .find(file =>
        file.getName == s"pekko-grpc-codegen_${scalaBinaryVersion.value}-${Dependencies.Versions.pekkoGrpc}-sources.jar"
      )
      .getOrElse(sys.error("Could not locate pekko-grpc-codegen sources jar for Twirl compatibility generation"))

    IO.delete(outputDir)
    IO.delete(extractDir)
    IO.createDirectory(outputDir)
    IO.unzip(sourceJar, extractDir)

    val generatedFiles = TemplateSources.map { relativePath =>
      val sourceFile    = extractDir / relativePath
      val outputFile    = outputDir / relativePath.replace(".template.scala", ".scala")
      val patchedSource = IO
        .read(sourceFile)
        .replace(" with _root_.play.twirl.api.Template", CompatMixin)
      IO.write(outputFile, patchedSource)
      outputFile
    }

    val traitFile = outputDir / "templates" / "TwirlFormatCompat.scala"
    IO.write(traitFile, CompatTraitSource)

    generatedFiles :+ traitFile
  }
}
