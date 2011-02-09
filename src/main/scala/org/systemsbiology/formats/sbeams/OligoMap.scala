package org.systemsbiology.formats.sbeams

import java.io._
import java.util.Arrays
import java.util.Comparator
import scala.collection.mutable.HashMap

/**
 * Classes for SBEAMS data interaction.
 */

/**
 * OligoMapDatabase accesses oligo map files in a designated directory.
 */
object OligoMapDatabase {
  val RefIndex = 4
  val VngIndex = 10
}
class OligoMapDatabase(oligoMapDir: File) {
  import OligoMapDatabase._

  def latest = {
    val files = mapFiles
    Arrays.sort(files, new Comparator[File] {
      def compare(file1: File, file2: File) = filenameCompare(file1.getName, file2.getName)
    })
    files(0)
  }
  
  private def filenameNumberSum(name: String) = {
    name.replace("halo_oligo_", "").replace(".map", "").split("-").map(str => Integer.parseInt(str)).sum
  }
  private def filenameCompare(name1: String, name2: String): Int = {
    filenameNumberSum(name2) - filenameNumberSum(name1)
  }

  def numMapFiles = mapFiles.length

  private def mapFiles = oligoMapDir.listFiles.filter(file => file.getName.matches("halo_oligo_.*.map"))

  def latestMap = {
    val input = new BufferedReader(new FileReader(latest))
    val map = new HashMap[String, String]
    try {
      var line = input.readLine // skip line 1
      while (line != null) {
        line = input.readLine
        if (line != null) {
          val fields = line.split("\t")
          map(fields(RefIndex)) = fields(VngIndex)
        }
      }
      map.toMap
    } finally {
      input.close
    }
  }
}
