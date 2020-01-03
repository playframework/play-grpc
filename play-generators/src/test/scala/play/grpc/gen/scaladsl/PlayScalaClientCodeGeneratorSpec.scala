/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.gen.scaladsl

import akka.grpc.gen.scaladsl.Service
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlayScalaClientCodeGeneratorSpec extends AnyWordSpec with Matchers {

  "The PlayScalaClientCodeGenerator" must {

    "choose the single package name" in {
      PlayScalaClientCodeGenerator
        .packageForSharedModuleFile(Seq(Service("a.b", "MyService", "???", Nil, false, false))) should ===("a.b")
    }

    "choose the longest common package name" in {
      PlayScalaClientCodeGenerator
        .packageForSharedModuleFile(
          Seq(
            Service("a.b.c", "MyService", "???", Nil, false, false),
            Service("a.b.e", "OtherService", "???", Nil, false, false),
          ),
        ) should ===("a.b")
    }

    "choose the root package if no common packages" in {
      PlayScalaClientCodeGenerator
        .packageForSharedModuleFile(
          Seq(
            Service("a.b.c", "MyService", "???", Nil, false, false),
            Service("c.d.e", "OtherService", "???", Nil, false, false),
          ),
        ) should ===("")
    }
  }

}
