package build.play.grpc

import sbt._
import sbt.Keys._

import Dependencies.Versions.publishedScalaVersions
import Dependencies.Versions.resolveScalaVersion
import Dependencies.Versions.scala213Version

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
    scalacOptions ++= {
      if (scalaVersion.value.startsWith("3.3.")) Seq("-Yfuture-lazy-vals") else Seq.empty
    },
    doc / javacOptions --= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    crossScalaVersions := publishedScalaVersions,
    scalaVersion       := resolveScalaVersion(sys.props.getOrElse("scala.version", scala213Version)),
  )

  val scalaVersionNumber = Def.setting(VersionNumber(scalaVersion.value))
}
