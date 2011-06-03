package org.systemsbiology.formats.common

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
  val RefIndex      = 4
  val GeneNameIndex = 9
  val VngIndex      = 10
}

case class GeneNameEntry(vngName: String, geneName: String)

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

  /**
   * Provides mapping from a SBEAMS gene name to VNG and gene function name.
   */
  def latestMap = {
    val input = new BufferedReader(new FileReader(latest))
    val map = new HashMap[String, GeneNameEntry]
    try {
      var line = input.readLine // skip line 1
      while (line != null) {
        line = input.readLine
        if (line != null) {
          val fields = line.split("\t")
          map(fields(RefIndex)) = GeneNameEntry(vngName = fields(VngIndex),
                                                geneName = fields(GeneNameIndex))
        }
      }
      map.toMap
    } finally {
      input.close
    }
  }
  /**
   * Provides a mapping from a VNG name to gene functional name.
   */
  def latestVng2GeneNameMap = {
    val input = new BufferedReader(new FileReader(latest))
    val map = new HashMap[String, String]
    try {
      var line = input.readLine // skip line 1
      while (line != null) {
        line = input.readLine
        if (line != null) {
          val fields = line.split("\t")
          map(fields(VngIndex)) = fields(GeneNameIndex)
        }
      }
      map.toMap
    } finally {
      input.close
    }
  }
}
