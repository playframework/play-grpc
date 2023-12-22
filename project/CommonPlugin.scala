package build.play.grpc

import sbt._
import sbt.Keys._

import Dependencies.Versions.scala213
import Dependencies.Versions.scala3

// WORKAROUND https://github.com/sbt/sbt/issues/2899
object CommonPlugin extends AutoPlugin {
  override def trigger = allRequirements

  private val akkaDeps =
    Seq("akka-actor", "akka-actor-typed", "akka-slf4j", "akka-serialization-jackson", "akka-stream", "akka-discovery")
  private val scala2Deps = Map(
    "com.typesafe.akka"            -> ("2.6.21", akkaDeps),
    "com.typesafe"                 -> ("0.6.1", Seq("ssl-config-core")),
    "com.fasterxml.jackson.module" -> ("2.14.3", Seq("jackson-module-scala")),
    "org.scala-lang.modules"       -> ("xxx", Seq("scala-parser-combinators")),
  )

  override def projectSettings = Seq(
    scalacOptions ++= {
      if (scalaVersionNumber.value.matchesSemVer(SemanticSelector("<=2.12")))
        List("-Yno-adapted-args")
      else
        Nil
    },
    doc / javacOptions --= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    Test / javaOptions ++= Seq("--add-exports=java.base/sun.security.x509=ALL-UNNAMED"),
    Test / fork        := true,
    crossScalaVersions := Seq(scala213, scala3),
    scalaVersion       := scala213,
    // Work around needed because akka-http 10.2.x is not published for Scala 3
    excludeDependencies ++=
      (if (scalaBinaryVersion.value == "3") {
         scala2Deps.flatMap(e => e._2._2.map(_ + "_3").map(ExclusionRule(e._1, _))).toSeq
       } else {
         Seq.empty
       }),
  )

  val scalaVersionNumber = Def.setting(VersionNumber(scalaVersion.value))
}
