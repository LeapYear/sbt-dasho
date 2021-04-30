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

import java.io.{BufferedWriter, File, FileWriter}

import scala.xml.{Elem, PrettyPrinter}

/**
  * Configuration file for DashO
  *
  * @param version DashO version
  * @param inputJar jar path to protect
  * @param outputJar protected jar path
  * @param mappingFile mapping file path
  * @param reportFile report path
  * @param classPaths classpaths to inspect
  */
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
      <classpath JDKHome={System.getProperty("java.home")} useJDKHome="true">
          {classPaths.map(new PathElement(_).toXml)}
      </classpath>
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

  def write(path: File): Unit = {
    val width = 250
    val pretty = new PrettyPrinter(width, 2)
    val out = new BufferedWriter(new FileWriter(path))
    try {
      out.write(pretty.format(toXml))
    } finally {
      out.close()
    }
  }
}
