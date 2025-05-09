addSbtPlugin("com.eed3si9n"      % "sbt-buildinfo"      % "0.13.1")
addSbtPlugin("com.typesafe.play" % "sbt-twirl"          % "1.6.9")
addSbtPlugin("de.heikoseeberger" % "sbt-header"         % "5.10.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"       % "2.5.4")
addSbtPlugin("com.github.sbt"    % "sbt-java-formatter" % "0.10.0")
addSbtPlugin(
  "com.lightbend.akka.grpc" % "sbt-akka-grpc" %
    (if (System.getProperty("USE_BSL", "false") == "true") "2.3.4" else "2.1.5") // Sync with docs/antora.yml
)

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.9.3")
