/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.gen.scaladsl

import com.google.protobuf.DescriptorProtos.ServiceOptions
import org.apache.pekko.grpc.gen.scaladsl.BadCitizen
import org.apache.pekko.grpc.gen.scaladsl.Service
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlayScalaClientCodeGeneratorSpec extends AnyWordSpec with Matchers {

  "The PlayScalaClientCodeGenerator" must {

    "choose the single package name" in {
      PlayScalaClientCodeGenerator
        .packageForSharedModuleFile(
          Seq(
            Service(
              "descriptor",
              "a.b",
              "MyService",
              "???",
              Nil,
              false,
              false,
              ServiceOptions.getDefaultInstance,
              None,
              BadCitizen.scalaCompatConstants()
            )
          ),
        ) should ===(
        "a.b",
      )
    }

    "choose the longest common package name" in {
      PlayScalaClientCodeGenerator
        .packageForSharedModuleFile(
          Seq(
            Service(
              "descriptor",
              "a.b.c",
              "MyService",
              "???",
              Nil,
              false,
              false,
              ServiceOptions.getDefaultInstance,
              None,
              BadCitizen.scalaCompatConstants()
            ),
            Service(
              "descriptor",
              "a.b.e",
              "OtherService",
              "???",
              Nil,
              false,
              false,
              ServiceOptions.getDefaultInstance,
              None,
              BadCitizen.scalaCompatConstants()
            ),
          ),
        ) should ===("a.b")
    }

    "choose the root package if no common packages" in {
      PlayScalaClientCodeGenerator
        .packageForSharedModuleFile(
          Seq(
            Service(
              "descriptor",
              "a.b.c",
              "MyService",
              "???",
              Nil,
              false,
              false,
              ServiceOptions.getDefaultInstance,
              None,
              BadCitizen.scalaCompatConstants()
            ),
            Service(
              "descriptor",
              "c.d.e",
              "OtherService",
              "???",
              Nil,
              false,
              false,
              ServiceOptions.getDefaultInstance,
              None,
              BadCitizen.scalaCompatConstants()
            ),
          ),
        ) should ===("")
    }
  }

}
