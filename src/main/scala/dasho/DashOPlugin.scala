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

import java.io.{BufferedWriter, File, FileWriter, IOException}

import sbt.{AutoPlugin, Compile, Def, Keys, PluginTrigger, Plugins, Runtime, Setting}
import sbt.plugins.JvmPlugin

import scala.sys.process.Process
import scala.xml.PrettyPrinter

/**
  * DashO plugin
  */
object DashOPlugin extends AutoPlugin {
  object autoImport extends DashOKeys
  import autoImport.{dashOHome, dashOVersion, protect}

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    dashOHome := sys.env("DASHO_HOME"),
    dashOVersion := "9.0.0",
    protect := dashOTask.value
  )
  override val trigger: PluginTrigger = noTrigger
  override val requires: Plugins = JvmPlugin

  @throws[RuntimeException]
  private def dashOTask = Def.task {
    val log = Keys.sLog.value

    // Get classPaths for DashO to inspect
    val classPaths: Seq[File] = (Keys.fullClasspath in Runtime).value.files

    // The location of the JAR and get the output names right.
    val inputArtifactPath = (Keys.artifactPath in Keys.packageBin in Compile).value
    val baseName = inputArtifactPath.getAbsolutePath.split("\\.(?=[^.]+$)").head
    val outputArtifactPath = new File(baseName + "-protected.jar")
    val mappingFile = new File(baseName + "-dashOMapping.txt")
    val reportFile = new File(baseName + "-dashOReport.txt")
    val configFile = baseName + ".dox"

    // Write DashO config
    log.info(s"Writing DashO config to $configFile")
    val width = 250
    val pretty = new PrettyPrinter(width, 2)
    val config = new DashOConfig(
      dashOVersion.value,
      inputArtifactPath,
      outputArtifactPath,
      mappingFile,
      reportFile,
      classPaths)
    writeFile(configFile, pretty.format(config.toXml))

    val jvm_location: String = {
      val javaExe = if (System.getProperty("os.name").startsWith("Win")) "java.exe" else "java"
      Seq(
        System.getProperties.getProperty("java.home"),
        "bin",
        javaExe
      ).mkString(File.separator)
    }

    // Run the DashO protection
    log.info("Protecting using DashO")
    val options = Seq("-jar", dashOHome.value + "/DashOPro.jar", configFile)
    log.debug("DashO command:")
    log.debug(jvm_location + " " + options.mkString(" "))
    val exitCode = Process(jvm_location, options) ! log
    if (exitCode != 0) sys.error(s"DashO failed with exit code [$exitCode]")
  }
  @throws[IOException]
  private def writeFile(canonicalFilename: String, text: String): Unit = {
    val file = new File(canonicalFilename)
    val out = new BufferedWriter(new FileWriter(file))
    out.write(text)
    out.close()
  }

}
