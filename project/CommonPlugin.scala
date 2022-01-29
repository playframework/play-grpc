package build.play.grpc

import sbt._
import sbt.Keys._
import Dependencies.Versions.scala212
import Dependencies.Versions.scala213

// WORKAROUND https://github.com/sbt/sbt/issues/2899
object CommonPlugin extends AutoPlugin {
  override def trigger = allRequirements

  override def projectSettings = Seq(
    scalacOptions ++= {
      if (scalaVersionNumber.value.matchesSemVer(SemanticSelector("<=2.12")))
        List("-Yno-adapted-args")
      else
        Nil
    },
    doc / javacOptions --= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    crossScalaVersions := Seq(scala212, scala213),
  )

  val scalaVersionNumber = Def.setting(VersionNumber(scalaVersion.value))
}
