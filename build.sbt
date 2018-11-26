val playGrpc = project in file(".")

aggregateProjects(
  testdata,
)

organization in ThisBuild := "com.lightbend.play"
     version in ThisBuild := "0.1.0-SNAPSHOT"

scalacOptions in ThisBuild ++= List(
  "-encoding", "utf8",
  "-deprecation", 
  "-feature", 
  "-unchecked", 
  "-Xlint",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
)

javacOptions in ThisBuild ++= List(
  "-Xlint:unchecked", 
  "-Xlint:deprecation",
)

val testdata = project
  .enablePlugins(AkkaGrpcPlugin)
  .settings(
    scalacOptions += "-Xlint:-unused,_",
    akkaGrpcGeneratedLanguages += AkkaGrpc.Java,
    akkaGrpcCodeGeneratorSettings -= "flat_package", // avoid Java+Scala fqcn conflicts
    skip in publish := true,
  )

cancelable in Global := true
