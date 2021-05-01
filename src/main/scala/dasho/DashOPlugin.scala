/*
 * Copyright (c) 2020 LeapYear
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dasho

import java.io.File
import sbt.Keys.packageBin
import sbt.{
  AutoPlugin,
  Compile,
  Def,
  Keys,
  MessageOnlyException,
  PluginTrigger,
  Plugins,
  Runtime,
  Setting,
  file
}
import sbt.plugins.JvmPlugin

import scala.sys.process.Process

/**
  * DashO plugin
  */
object DashOPlugin extends AutoPlugin {
  object autoImport extends DashOKeys
  import autoImport.{dashOHome, dashOVersion, protect, jdkHome}

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    dashOHome := sys.env.get("DASHO_HOME") map file,
    jdkHome := sys.env.get("JDK_HOME") map file,
    dashOVersion := "9.0.0",
    protect := Def.sequential(Compile / packageBin, dashOTask).value
  )
  override val trigger: PluginTrigger = noTrigger
  override val requires: Plugins = JvmPlugin

  private def dashOTask = Def.task {
    val log = Keys.sLog.value
    val dashOHomeSetting =
      dashOHome.value.getOrElse(throw new MessageOnlyException("dashOHome is not set"))

    // Get classPaths for DashO to inspect
    val classPaths: Seq[File] = (Keys.fullClasspath in Runtime).value.files

    // The location of the JAR and get the output names right.
    val inputArtifactPath = (Keys.artifactPath in Keys.packageBin in Compile).value
    val baseName = inputArtifactPath.getAbsolutePath.split("\\.(?=[^.]+$)").head
    val configFile = new File(baseName + ".dox")
    val protectedJar = new File(baseName + "-protected.jar")

    // Write DashO config
    log.info(s"Writing DashO config to $configFile")
    new DashOConfig(
      dashOVersion.value,
      inputArtifactPath,
      protectedJar,
      new File(baseName + "-dashOMapping.txt"),
      new File(baseName + "-dashOReport.txt"),
      classPaths,
      jdkHome.value).write(configFile)

    // Run the DashO protection
    log.info("Protecting using DashO")
    runDashOJar(configFile, new File(dashOHomeSetting + "/DashOPro.jar"), log)
    protectedJar
  }

  private def runDashOJar(configFile: File, dashOJar: File, log: sbt.util.Logger): Unit = {
    val jvm_location: String = {
      val javaExe = if (System.getProperty("os.name").startsWith("Win")) "java.exe" else "java"
      Seq(
        System.getProperties.getProperty("java.home"),
        "bin",
        javaExe
      ).mkString(File.separator)
    }
    val options = Seq("-jar", dashOJar.toString, configFile.toString)
    log.debug("DashO command:")
    log.debug(jvm_location + " " + options.mkString(" "))
    val exitCode = Process(jvm_location, options) ! log
    if (exitCode != 0) sys.error(s"DashO failed with exit code [$exitCode]")
  }

}
