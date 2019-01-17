package build.play.grpc

import sbt._
import sbt.Keys._

import akka.grpc.sbt.AkkaGrpcPlugin
import akka.grpc.sbt.AkkaGrpcPlugin.autoImport._
import com.lightbend.sbt.javaagent.JavaAgent
import com.lightbend.sbt.javaagent.JavaAgent.autoImport.javaAgents
import sbtprotoc.ProtocPlugin
import sbtprotoc.ProtocPlugin.autoImport.PB

// helper to define projects that test the plugin infrastructure
object ProjectExtensions {
  implicit class AddPluginTest(project: Project) {
    /** Add settings to test the sbt-plugin in-process */
    def pluginTestingSettings: Project =
      project
        .enablePlugins(JavaAgent)
        .settings(
          javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.9" % Test,
          libraryDependencies += Dependencies.Compile.akkaGrpcRuntime,
          // TODO: why only Java?
          akkaGrpcGeneratedLanguages += AkkaGrpc.Java,
          akkaGrpcCodeGeneratorSettings -= "flat_package", // avoid Java+Scala fqcn conflicts
        )
        .enablePlugins(AkkaGrpcPlugin)
  }
}
