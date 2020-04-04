scalaVersion     := "2.12.10"
version          := "0.1.0-SNAPSHOT"
organization     := "io.leapyear"
organizationName := "LeapYear"

lazy val root = (project in file("."))
  .settings(
    name := "sbt-dasho",
    organization := "io.leapyear",
    sbtPlugin := true,
    version := "0.1-SNAPSHOT",
    scriptedBufferLog := false
  )
