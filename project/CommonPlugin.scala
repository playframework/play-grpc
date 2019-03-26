import sbt._
import sbt.Keys._

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
  )

  val scalaVersionNumber = Def.setting(VersionNumber(scalaVersion.value))
}
