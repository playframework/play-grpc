# Releasing

## Releasing

1. Update the version number in the `play-grpc-xx-stable` project name in the [whitesource web UI](https://saas.whitesourcesoftware.com)
    - For example you'd call the project `play-grpc-0.5-stable`
1. Create a [new release](https://github.com/playframework/play-grpc/releases/new) with the next tag version (e.g. `v0.5`), title and release description including notable changes mentioning external contributors.
1. Travis CI will start a [CI build](https://travis-ci.org/playframework/play-grpc/builds) for the new tag and publish to [Bintray](https://bintray.com/playframework/maven) that needs to be synced with Maven Central.
1. Login to [Bintray](https://bintray.com/playframework/maven/play-grpc) and sync to Maven Central.
