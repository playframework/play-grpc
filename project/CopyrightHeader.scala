/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package build.play.grpc

import sbt._

import sbtheader.CommentCreator
import sbtheader.CommentStyle
import sbtheader.HeaderPlugin
import Keys._

object CopyrightHeader extends AutoPlugin {
  import HeaderPlugin.autoImport._

  override def requires = HeaderPlugin
  override def trigger  = allRequirements

  override def buildSettings = Seq(headerEmptyLine := false)

  override def projectSettings = Def.settings(
    Seq(Compile, Test).flatMap { config =>
      inConfig(config)(
        Seq(
          headerLicense := Some(
            HeaderLicense.Custom(
              "Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>",
            ),
          ),
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
