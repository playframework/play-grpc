/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.gen.scaladsl

import scala.collection.immutable

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import org.apache.pekko.grpc.gen.scaladsl.ScalaCodeGenerator
import org.apache.pekko.grpc.gen.scaladsl.ScalaServerCodeGenerator
import org.apache.pekko.grpc.gen.scaladsl.Service
import org.apache.pekko.grpc.gen.Logger
import templates.PlayScala.txt._

class PlayScalaServerCodeGenerator extends ScalaCodeGenerator {

  override def name: String = "play-grpc-server-scala"

  override def perServiceContent = super.perServiceContent + generatePlainRouter + generatePowerRouter

  private val generatePlainRouter: (Logger, Service) => immutable.Seq[CodeGeneratorResponse.File] =
    (logger, service) => {
      val b = CodeGeneratorResponse.File.newBuilder()

      if (service.usePlayActions) b.setContent(RouterUsingActions(service, powerApis = false).body)
      else b.setContent(Router(service, powerApis = false).body)

      b.setName(s"${service.packageDir}/Abstract${service.name}Router.scala")
      logger.info(s"Generating Play gRPC service play router for ${service.packageName}.${service.name}")
      immutable.Seq(b.build)
    }

  private val generatePowerRouter: (Logger, Service) => immutable.Seq[CodeGeneratorResponse.File] =
    (logger, service) => {
      if (service.serverPowerApi) {
        val b = CodeGeneratorResponse.File.newBuilder()

        if (service.usePlayActions) b.setContent(RouterUsingActions(service, powerApis = true).body)
        else b.setContent(Router(service, powerApis = true).body)

        b.setName(s"${service.packageDir}/Abstract${service.name}PowerApiRouter.scala")
        logger.info(s"Generating Pekko gRPC service power API play router for ${service.packageName}.${service.name}")
        immutable.Seq(b.build)
      } else immutable.Seq.empty,
    }
}
object PlayScalaServerCodeGenerator extends PlayScalaServerCodeGenerator
