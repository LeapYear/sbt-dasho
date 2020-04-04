/*
 * Copyright (c) 2019 LeapYear Technologies.
 */

package dasho

import java.io.File

import scala.xml.Elem

private final class DashOConfig(
    private val version: String,
    private val inputJar: File,
    private val outputJar: File,
    private val mappingFile: File,
    private val reportFile: File,
    private val classPaths: Seq[File]) {

  def toXml: Elem =
    <dasho version={version}>
      <inputpath><pathelement location={inputJar.getAbsolutePath} /></inputpath>
      <entrypoints>
        <library public="off">
          <jar path={inputJar.getAbsolutePath} />
        </library>
      </entrypoints>
      <output>
        <jar path={outputJar.getAbsolutePath} />
      </output>
      <!-- third-party packages or jars that should also be analyzed by DashO -->
      <classpath>{classPaths.map(new PathElement(_).toXml)}</classpath>
      <report path={reportFile.getAbsolutePath} />
      <!-- The removal section specifies granularity for class/method/field/metadata removal -->
      <removal classes="unused-non-public" members="unused" />
      <!-- Remove debug information -->
      <debug types="All" />
      <!-- renaming section enables Dasho to rename classes/methods/fields to short names -->
      <renaming option="on" renameAnnotations="on">
        <class-options randomize="true"/>
        <member-options randomize="true"/>
        <class-options minlength="4"/>
        <mapping>
          <mapreport path={mappingFile.getAbsolutePath} />
        </mapping>
      </renaming>
      <!-- Dasho bytecode optimization, may or may not be useful, off for now -->
      <optimization option="off" />
      <!-- Control flow obfuscation -->
      <controlflow option="on" tryCatch="on" blockSplitting="on" />
    </dasho>

  class PathElement(final val classPath: File) {
    def toXml: Elem = <pathelement location={classPath.getAbsolutePath} />
  }
}
