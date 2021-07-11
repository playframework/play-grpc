/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */

package build.play.grpc

import sbt._
import Keys._
import de.heikoseeberger.sbtheader.CommentCreator
import de.heikoseeberger.sbtheader.CommentStyle
import de.heikoseeberger.sbtheader.HeaderPlugin

object CopyrightHeader extends AutoPlugin {
  import HeaderPlugin.autoImport._

  override def requires = HeaderPlugin
  override def trigger  = allRequirements

  override def buildSettings = Seq(headerEmptyLine := false)

  override def projectSettings = Def.settings(
    Seq(Compile, Test).flatMap { config =>
      inConfig(config)(
        Seq(
          headerLicense := Some(HeaderLicense.Custom("Copyright (C) Lightbend Inc. <https://www.lightbend.com>")),
          headerMappings := headerMappings.value ++ Map(
            HeaderFileType.scala  -> HeaderCommentStyle.cStyleBlockComment,
            HeaderFileType.java   -> HeaderCommentStyle.cStyleBlockComment,
            HeaderFileType("txt") -> HeaderCommentStyle.twirlStyleBlockComment,
          ),
          (headerCreate / unmanagedResourceDirectories) += baseDirectory.value / "src" / "main" / "twirl",
        ),
      )
    },
  )

}
