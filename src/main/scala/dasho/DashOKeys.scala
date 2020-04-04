/*
 * Copyright (c) 2019 LeapYear Technologies.
 */

package dasho

import sbt.{settingKey, taskKey}

trait DashOKeys {
  lazy val dashOHome = settingKey[String]("Location of DashO")
  lazy val dashOVersion = settingKey[String]("Version of DashO")
  lazy val protect = taskKey[Unit]("Protect jar using DashO")
}
