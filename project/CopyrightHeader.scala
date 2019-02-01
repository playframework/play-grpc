/*
 * Copyright (C) 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package build.play.grpc

import sbt._, Keys._
import de.heikoseeberger.sbtheader.{ CommentCreator, CommentStyle, HeaderPlugin }

object CopyrightHeader extends AutoPlugin {
  import HeaderPlugin.autoImport._

  override def requires = HeaderPlugin
  override def trigger = allRequirements

  override def buildSettings = Seq(headerEmptyLine := false)

  override def projectSettings = Def.settings(
    Seq(Compile, Test).flatMap { config =>
      inConfig(config)(
        Seq(
          headerLicense := Some(HeaderLicense.Custom(headerFor(CurrentYear))),
          headerMappings := headerMappings.value ++ Map(
            HeaderFileType.scala       -> cStyleComment,
            HeaderFileType.java        -> cStyleComment,
            HeaderFileType("txt") -> twirlStyleBlockComment,
          ),
          unmanagedResourceDirectories in headerCreate += baseDirectory.value / "src" / "main" / "twirl"
        )
      )
    }
  )

  val CurrentYear = java.time.Year.now.getValue.toString
  val CopyrightPattern =
    "Copyright \\([Cc]\\) (\\d{4}(-\\d{4})?) (?:Lightbend|Typesafe) Inc. <.*>".r
  val CopyrightHeaderPattern = s"(?s).*${CopyrightPattern}.*".r

  def headerFor(yearRange: String): String =
    s"Copyright (C) $yearRange Lightbend Inc. <https://www.lightbend.com>"

  /** Updates the year range in the Lightbend copyright line */
  private class LightbendCommentCreator(commentCreator: CommentCreator) extends CommentCreator {
    private def updatedYearRange(header: String): Option[String] = PartialFunction.condOpt(header) {
      case CopyrightHeaderPattern(CurrentYear, null) => CurrentYear
      case CopyrightHeaderPattern(years, null)       => years + "-" + CurrentYear
      case CopyrightHeaderPattern(years, endYears)   => years.replace(endYears, "-" + CurrentYear)
    }

    override def apply(text: String, existingText: Option[String]): String = {
      val newText = commentCreator(text, existingText)
      existingText
        .flatMap(updatedYearRange)
        .map(yearRange => CopyrightPattern.replaceFirstIn(newText, headerFor(yearRange)))
        .getOrElse(newText)
        .trim
    }
  }

  private def updateCommentCreator(style: CommentStyle) =
    style.copy(commentCreator = new LightbendCommentCreator(style.commentCreator))

  val cStyleComment          = updateCommentCreator(HeaderCommentStyle.cStyleBlockComment)
  val twirlStyleBlockComment = updateCommentCreator(HeaderCommentStyle.twirlStyleBlockComment)
}
