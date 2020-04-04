lazy val root = (project in file("."))
  .enablePlugins(DashOPlugin)
  .settings(
    version := "0.1",
    scalaVersion := "2.12.10"
  )
