addSbtPlugin("com.dwijnand"       % "sbt-dynver"         % "2.1.0")
addSbtPlugin("com.dwijnand"       % "sbt-travisci"       % "1.1.3")
addSbtPlugin("com.eed3si9n"       % "sbt-assembly"       % "0.14.6")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo"      % "0.8.0")
addSbtPlugin("com.lightbend"      % "sbt-whitesource"    % "0.1.12")
addSbtPlugin("com.lightbend.akka" % "sbt-paradox-akka"   % "0.12")
addSbtPlugin("com.lightbend.sbt"  % "sbt-javaagent"      % "0.1.4")
addSbtPlugin("com.thesamet"       % "sbt-protoc"         % "0.99.18")
addSbtPlugin("com.typesafe.sbt"   % "sbt-git"            % "0.9.3")
addSbtPlugin("com.typesafe.sbt"   % "sbt-twirl"          % "1.3.13")
addSbtPlugin("de.heikoseeberger"  % "sbt-header"         % "5.1.0")
addSbtPlugin("org.foundweekends"  % "sbt-bintray"        % "0.5.4")
addSbtPlugin("com.lightbend.sbt"  % "sbt-java-formatter" % "0.4.1")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"       % "2.0.0-RC5")

addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "0.6.0")

// Only needed for akka, akka-grpc ,...  snapshots
// See also build.sbt
resolvers += Resolver.bintrayRepo("akka", "maven")
