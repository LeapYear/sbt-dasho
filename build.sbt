scalaVersion := "2.12.10"
version := "0.1.0-SNAPSHOT"
organization := "io.leapyear"
organizationName := "LeapYear"
licenses += ("MIT", new URL("https://opensource.org/licenses/MIT"))
startYear := Some(2020)

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin, AutomateHeaderPlugin)
  .settings(
    organization := "io.leapyear",
    name := "sbt-dasho",
    sbtPlugin := true,
    version := "0.1-SNAPSHOT",
    // scripted-plugin
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    watchSources ++= { (sourceDirectory.value ** "*").get }
  )
