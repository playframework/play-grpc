addSbtPlugin("com.dwijnand"       % "sbt-dynver"       % "4.0.0")
addSbtPlugin("com.dwijnand"       % "sbt-travisci"     % "1.2.0")
addSbtPlugin("com.eed3si9n"       % "sbt-assembly"     % "0.14.10")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo"    % "0.9.0")
addSbtPlugin("com.lightbend"      % "sbt-whitesource"  % "0.1.18")
addSbtPlugin("com.lightbend.akka" % "sbt-paradox-akka" % "0.32")
addSbtPlugin("com.lightbend.sbt"  % "sbt-javaagent"    % "0.1.4")
addSbtPlugin("com.typesafe.sbt"   % "sbt-git"          % "1.0.0")
addSbtPlugin("com.typesafe.sbt"   % "sbt-twirl"        % "1.5.0")
addSbtPlugin("de.heikoseeberger"  % "sbt-header"       % "5.4.0")
addSbtPlugin("org.foundweekends"  % "sbt-bintray"      % "0.5.6")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.3.2")

// This version must be in sync with the version of "akkaGrpc" in "project/Dependencies.scala"
addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "0.8.4")

// Only needed for akka, akka-grpc ,...  snapshots
// See also build.sbt
//resolvers += Resolver.bintrayRepo("akka", "maven")
