addSbtPlugin("com.dwijnand"       % "sbt-dynver"       % "4.1.1")
addSbtPlugin("com.dwijnand"       % "sbt-travisci"     % "1.2.0")
addSbtPlugin("com.eed3si9n"       % "sbt-assembly"     % "0.14.10")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo"    % "0.9.0")
addSbtPlugin("com.lightbend"      % "sbt-whitesource"  % "0.1.18")
addSbtPlugin("com.lightbend.akka" % "sbt-paradox-akka" % "0.34")
addSbtPlugin("com.lightbend.sbt"  % "sbt-javaagent"    % "0.1.4")
addSbtPlugin("com.typesafe.sbt"   % "sbt-twirl"        % "1.5.0")
addSbtPlugin("de.heikoseeberger"  % "sbt-header"       % "5.6.0")
addSbtPlugin("org.foundweekends"  % "sbt-bintray"      % "0.5.6")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.3.4")

addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "1.0.2")

// Only needed for akka, akka-grpc ,...  snapshots
// See also build.sbt
//resolvers += Resolver.bintrayRepo("akka", "maven")
