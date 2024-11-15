addSbtPlugin("com.eed3si9n"      % "sbt-buildinfo"      % "0.12.0")
addSbtPlugin("com.typesafe.play" % "sbt-twirl"          % "1.6.8")
addSbtPlugin("de.heikoseeberger" % "sbt-header"         % "5.10.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"       % "2.5.2")
addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.8.0")
addSbtPlugin(
  "com.lightbend.akka.grpc" % "sbt-akka-grpc" %
    (if (System.getProperty("USE_BSL", "false") == "true") "2.3.4" else "2.1.5") // Sync with docs/antora.yml
)

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.9.0")
