lazy val root = (project in file("."))
  .enablePlugins(DashOPlugin)
  .settings(
    version := "0.1",
    scalaVersion := "2.12.10",
    jdkHome := sys.env.get("JAVA_HOME") map file,
    dashOVersion := "11.1.0"
  )
