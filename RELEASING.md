# Releasing

1. Create a [new GitHub release](https://github.com/playframework/play-grpc/releases/new)
    - Tag version: *follow the version tagging convention*, e.g. v1.2.0
    - Release title: 1.2.0
    - Release description: include notable changes mentioning external contributors
1. Follow the [tag build](https://travis-ci.com/playframework/play-grpc/branches) on Travis CI until complete
1. [Sync](https://bintray.com/playframework/maven/play-grpc/_latestVersion#central) from Bintray to Maven Central
