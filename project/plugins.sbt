addSbtPlugin("com.eed3si9n"            % "sbt-buildinfo"      % "0.11.0")
addSbtPlugin("com.lightbend.akka"      % "sbt-paradox-akka"   % "0.44")
addSbtPlugin("com.typesafe.sbt"        % "sbt-twirl"          % "1.5.1")
addSbtPlugin("de.heikoseeberger"       % "sbt-header"         % "5.10.0")
addSbtPlugin("org.scalameta"           % "sbt-scalafmt"       % "2.5.2")
addSbtPlugin("com.lightbend.sbt"       % "sbt-java-formatter" % "0.8.0")
addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc"      % "2.1.5") // Sync with docs/antora.yml
addSbtPlugin("com.github.sbt"          % "sbt-ci-release"     % "1.5.12")

// To solve org.scala-lang.modules:scala-xml_2.12:2.1.0 cross dependency issues during metabuild
libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
)
