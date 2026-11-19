/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.gen.scaladsl

import com.google.protobuf.DescriptorProtos.ServiceOptions
import org.apache.pekko.grpc.gen.scaladsl.BadCitizen
import org.apache.pekko.grpc.gen.scaladsl.Service
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlayScalaServerCodeGeneratorSpec extends AnyWordSpec with Matchers {
  "The Scala server router templates" must {
    for {
      scala3Sources <- Seq(false, true)
      powerApis     <- Seq(false, true)
      useActions    <- Seq(false, true)
    } {
      s"pass the actor system correctly with scala3Sources=$scala3Sources, powerApis=$powerApis and useActions=$useActions" in {
        val service = Service(
          "descriptor",
          "example",
          "GreeterService",
          "example.GreeterService",
          Nil,
          powerApis,
          useActions,
          ServiceOptions.getDefaultInstance,
          None,
          BadCitizen.scalaCompatConstants(scala3Sources),
        )
        val source =
          if (useActions) templates.PlayScala.txt.RouterUsingActions(service, powerApis).body
          else templates.PlayScala.txt.Router(service, powerApis).body
        val name           = if (powerApis) "GreeterServicePowerApi" else "GreeterService"
        val prefix         = if (useActions) "serviceName" else "GreeterService.name"
        val systemArgument = if (scala3Sources) "using system" else "system"
        source should include(s"${name}Handler(this, $prefix, eHandler)($systemArgument)")
      }
    }
  }
}
