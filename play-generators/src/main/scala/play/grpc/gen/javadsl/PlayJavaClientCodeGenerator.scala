/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.gen.javadsl

import scala.collection.immutable

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import org.apache.pekko.grpc.gen.javadsl.JavaCodeGenerator
import org.apache.pekko.grpc.gen.javadsl.Service
import org.apache.pekko.grpc.gen.Logger
import play.grpc.gen.scaladsl.PlayScalaClientCodeGenerator
import templates.PlayJava.txt.ClientProvider
import templates.PlayJava.txt.PekkoGrpcClientModule

object PlayJavaClientCodeGenerator extends PlayJavaClientCodeGenerator

class PlayJavaClientCodeGenerator extends JavaCodeGenerator {
  override def name: String = "play-grpc-client-java"

  override def perServiceContent = super.perServiceContent + generateClientProvider

  private val generateClientProvider: (Logger, Service) => immutable.Seq[CodeGeneratorResponse.File] =
    (logger, service) => {
      val b = CodeGeneratorResponse.File.newBuilder()
      b.setContent(ClientProvider(service).body)
      b.setName(s"${service.packageName.replace('.', '/')}/${service.name}ClientProvider.java")
      logger.info(s"Generating Play gRPC client provider for ${service.packageName}.${service.name}")
      immutable.Seq(b.build)
    }

  override def staticContent(logger: Logger, allServices: Seq[Service]): Set[CodeGeneratorResponse.File] = {
    if (allServices.nonEmpty) {
      val packageName = packageForSharedModuleFile(allServices)
      val b           = CodeGeneratorResponse.File.newBuilder()
      b.setContent(PekkoGrpcClientModule(packageName, allServices).body)
      b.setName(s"${packageName.replace('.', '/')}/${PlayScalaClientCodeGenerator.ClientModuleName}.java")
      val set = Set(b.build)
      logger.info(
        s"Generated [${packageName}.${PlayScalaClientCodeGenerator.ClientModuleName}] add it to play.modules.enabled and a section " +
          "with Pekko gRPC client config under org.apache.pekko.grpc.client.\"servicepackage.ServiceName\" to be able to inject " +
          "client instances.",
      )
      set

    } else Set.empty
  }

  private[play] def packageForSharedModuleFile(allServices: Seq[Service]): String =
    // single service or all services in single package - use that
    if (allServices.forall(_.packageName == allServices.head.packageName)) allServices.head.packageName
    else {
      // try to find longest common prefix
      allServices.tail.foldLeft(allServices.head.packageName)((packageName, service) =>
        if (packageName == service.packageName) packageName
        else PlayScalaClientCodeGenerator.commonPackage(packageName, service.packageName),
      )
    }

}
