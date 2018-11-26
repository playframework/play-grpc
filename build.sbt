val playGrpc = project in file(".")

aggregateProjects(
  playTestdata,
)

organization in ThisBuild := "com.lightbend.play"
     version in ThisBuild := "0.1.0-SNAPSHOT"

scalacOptions in ThisBuild ++= List(
  "-encoding", "utf8",
  "-deprecation", "-feature", "-unchecked", "-Xlint",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
)

javacOptions in ThisBuild ++= List("-Xlint:unchecked", "-Xlint:deprecation")

val playTestdata = project
  .enablePlugins(AkkaGrpcPlugin)
  .settings(
    scalacOptions += "-Xlint:-unused,_",
    skip in publish := true,
  )
