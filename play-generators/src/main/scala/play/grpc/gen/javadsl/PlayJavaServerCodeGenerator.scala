/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.gen.javadsl

import scala.collection.immutable

import akka.grpc.gen.javadsl.JavaCodeGenerator
import akka.grpc.gen.javadsl.Service
import akka.grpc.gen.Logger
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import templates.PlayJavaServer.txt.Router
import templates.PlayJavaServer.txt.RouterUsingActions

class PlayJavaServerCodeGenerator extends JavaCodeGenerator {
  override def name: String = "play-grpc-server-java"

  override def perServiceContent = super.perServiceContent + generatePlainRouter + generatePowerRouter

  private val generatePlainRouter: (Logger, Service) => immutable.Seq[CodeGeneratorResponse.File] =
    (logger, service) => {
      val b = CodeGeneratorResponse.File.newBuilder()

      if (service.usePlayActions) b.setContent(RouterUsingActions(service, powerApis = false).body)
      else b.setContent(Router(service, powerApis = false).body)

      b.setName(s"${service.packageDir}/Abstract${service.name}Router.java")
      logger.info(s"Generating Play gRPC service play router for ${service.packageName}.${service.name}")
      immutable.Seq(b.build)
    }

  private val generatePowerRouter: (Logger, Service) => immutable.Seq[CodeGeneratorResponse.File] =
    (logger, service) => {
      if (service.serverPowerApi) {
        val b = CodeGeneratorResponse.File.newBuilder()

        if (service.usePlayActions) b.setContent(RouterUsingActions(service, powerApis = true).body)
        else b.setContent(Router(service, powerApis = true).body)

        b.setName(s"${service.packageDir}/Abstract${service.name}PowerApiRouter.java")
        logger.info(s"Generating Akka gRPC service power API play router for ${service.packageName}.${service.name}")
        immutable.Seq(b.build)
      } else immutable.Seq.empty,
    }
}
object PlayJavaServerCodeGenerator extends PlayJavaServerCodeGenerator
