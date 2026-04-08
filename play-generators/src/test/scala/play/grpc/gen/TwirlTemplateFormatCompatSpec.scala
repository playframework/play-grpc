/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.gen

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TwirlTemplateFormatCompatSpec extends AnyWordSpec with Matchers {

  private val templateModuleClasses = Seq(
    "templates.JavaClient.txt.GenMethodImports$",
    "templates.JavaClient.txt.ClientPowerApi$",
    "templates.JavaClient.txt.Client$",
    "templates.ScalaServer.txt.Handler$",
    "templates.ScalaServer.txt.PowerApiTrait$",
    "templates.ScalaClient.txt.Client$",
    "templates.JavaServer.txt.PowerApiInterface$",
    "templates.JavaServer.txt.Handler$",
    "templates.JavaCommon.txt.ApiInterface$",
    "templates.ScalaCommon.txt.ApiTrait$",
    "templates.ScalaCommon.txt.Marshallers$",
  )

  "The generated Twirl compatibility shadows" should {
    "keep the legacy format accessor available on all Pekko template modules" in {
      templateModuleClasses.foreach { moduleClassName =>
        val moduleClass = Class.forName(moduleClassName)
        val module      = moduleClass.getField("MODULE$").get(null)
        val format      = moduleClass.getMethod("format").invoke(module)
        val twirlFormat = moduleClass.getMethod("$twirl__format").invoke(module)

        (format should be).theSameInstanceAs(twirlFormat)
      }
    }
  }
}
