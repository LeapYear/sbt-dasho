/*
 * Copyright (c) 2019 LeapYear Technologies.
 */
package dasho

import java.io.{BufferedWriter, File, FileWriter, IOException}

import sbt.{AutoPlugin, Compile, Def, Keys, Plugins, PluginTrigger, Runtime, Setting}
import sbt.plugins.JvmPlugin

import scala.sys.process.Process
import scala.xml.PrettyPrinter

object DashOPlugin extends AutoPlugin {
  object autoImport extends DashOKeys
  import autoImport.{dashOHome, dashOVersion, protect}

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
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

    // Run the DashO protection
    log.info("Protecting using DashO")
    val options = Seq("-jar", dashOHome.value + "/DashOPro.jar", configFile)
    log.debug("DashO command:")
    log.debug("java " + options.mkString(" "))
    val exitCode = Process("java", options) ! log
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
