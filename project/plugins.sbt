resolvers += Resolver.sonatypeCentralSnapshots

addSbtPlugin("com.eed3si9n"            % "sbt-buildinfo"         % "0.13.1")
addSbtPlugin("org.playframework.twirl" % "sbt-twirl"             % "2.1.0-M9+129-bab5ac69-SNAPSHOT")
addSbtPlugin("com.github.sbt"          % "sbt-header"            % "5.11.0")
addSbtPlugin("org.scalameta"           % "sbt-scalafmt"          % "2.6.2")
addSbtPlugin("com.github.sbt"          % "sbt-java-formatter"    % "0.13.1")
addSbtPlugin("org.apache.pekko"        % "pekko-grpc-sbt-plugin" % "2.0.0-M2") // Sync with docs/antora.yml
addSbtPlugin("com.github.sbt"          % "sbt-ci-release"        % "1.12.1")
