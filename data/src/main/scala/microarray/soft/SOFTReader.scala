package org.systemsbiology.formats.microarray.soft

import java.io.{BufferedReader, File, FileReader, FileOutputStream}
import scala.collection.mutable.{ArrayBuffer, HashMap, HashSet}

import java.util.regex.Pattern
import java.util.logging._

import org.apache.commons.net.ftp._

case class Platform(name: String, ids: Seq[String],
                    id2RowMap: Map[String, Int],
                    geneMap: Map[String, String])
case class Measurement(idref: String, value: String)
case class SampleData(name: String, values: Seq[Measurement])
case class DataMatrix(rowNames: Seq[String],
                      sampleNames: Seq[String],
                      values: Array[Array[Double]]) {
  def numRows = rowNames.length
  def numColumns = sampleNames.length
}

object SOFTReader {
  val Log = Logger.getLogger("SOFTReader")

  private def readPlatform(platformLine: String, in: BufferedReader,
                           geneColumnNames: Seq[String]): Platform = {
    val geneMap = new HashMap[String, String]
    val id2RowMap = new HashMap[String, Int]
    val ids = new ArrayBuffer[String]
    val platformName = platformLine.split("=")(1).trim
    val values = new ArrayBuffer[Seq[String]]
    var line = in.readLine
    var row = 0

    while (line != null && !line.startsWith("!platform_table_begin")) line = in.readLine
    if (line == null) return null
    line = in.readLine // read the header
    val header = line.split("\t").toSeq
    val idCol = header.indexOf("ID")

    // Filter the gene column according to the priority list
    var geneCol = -1
    var geneColIndex = 0
    var continueLoop = geneColIndex < geneColumnNames.length
    while (continueLoop) {
      val geneColumnName = geneColumnNames(geneColIndex)
      geneCol = header.indexOf(geneColumnName)
      geneColIndex += 1
      continueLoop = geneCol < 0 && geneColIndex < geneColumnNames.length
    }

    if (idCol == -1) {
      Log.severe("could not find a ID column")
      return null
    }
    if (geneCol == -1) {
      Log.severe("could not find a gene column")
      return null
    }

    line = in.readLine

    while (!line.startsWith("!platform_table_end")) {
      val comps = line.split("\t")
      if (comps(geneCol) != "") {
        ids += comps(idCol)
        geneMap(comps(idCol)) = comps(geneCol)
        id2RowMap(comps(idCol)) = row
        row += 1
      } else {
        // skip the platform row for non-mappable genes
        Log.warning("platform: '%s' - id '%s' does not map to a gene -> ignoring".format(
          platformName, comps(idCol)))
      }
      line = in.readLine
    }
    Platform(platformName, ids, id2RowMap.toMap, geneMap.toMap)
  }

  private def readSample(sampleLine: String, in: BufferedReader,
                         platform: Platform) = {
    val sampleName = sampleLine.split("=")(1).trim
    val values = new ArrayBuffer[Measurement]

    var line = in.readLine
    while (!line.startsWith("!sample_table_begin")) line = in.readLine
    line = in.readLine // read the header
    val header = line.split("\t").toSeq
    val idRefCol = header.indexOf("ID_REF")
    val valueCol = header.indexOf("VALUE")

    line = in.readLine
    while (!line.startsWith("!sample_table_end")) {
      val comps = line.split("\t")
      val idref = comps(idRefCol)
      if (platform.id2RowMap.contains(idref)) {
        values += Measurement(comps(idRefCol), comps(valueCol))
      }
      line = in.readLine
    }
    SampleData(sampleName, values)
  }

  def read(in: BufferedReader, geneColumnNames: Seq[String]=List("ORF")) = {
    val platforms = new ArrayBuffer[Platform]
    val samples = new ArrayBuffer[SampleData]
    var line = in.readLine
    while (line != null) {
      if (line.startsWith("^PLATFORM")) {
        val platform = readPlatform(line, in, geneColumnNames)
        if (platform != null) platforms += platform
      }
      if (platforms.length > 0 && line.startsWith("^SAMPLE")) {
        samples += readSample(line, in, platforms(0))
      }
      line = in.readLine
    }
    if (platforms.length > 0) {
      val platform = platforms(0)
      Log.info("Merging platform '%s' into an array of %dx%d".format(
             platform.name, platform.ids.length, samples.length))
      val array = Array.ofDim[Double](platform.ids.length,
                                      samples.length)
      for (col <- 0 until samples.length) {
        samples(col).values.foreach { value =>
          val row = platform.id2RowMap(value.idref)
          try {
            array(row)(col) = value.value.toDouble
          } catch {
            case _: Throwable => array(row)(col) = java.lang.Double.NaN
          }
        }
      }
      DataMatrix(platform.ids.map{id => platform.geneMap(id) },
                 samples.map(_.name),
                 array)
    } else {
      Log.warning("no valid platform found")
      null
    }
  }

  def download(ftpURL: String, cacheDir: File): File = {
    val pathComps = ftpURL.replaceAll("ftp://", "").split("/").toSeq
    val server = pathComps(0)
    val path = pathComps.tail.mkString("/")
    val filename = pathComps(pathComps.length - 1)
    val cacheFile = new File(cacheDir, filename)

    if (cacheFile.exists) {
      Log.info("File '%s' was already downloaded.".format(filename))
      cacheFile
    } else {
      Log.info("downloading '%s' (%s) from server '%s'...".format(path, filename, server))
      var ftp: FTPClient = null
      try {
        ftp = new FTPClient
        ftp.connect(server)
        val reply = ftp.getReplyCode
        if (FTPReply.isPositiveCompletion(reply)) {
          if (ftp.login("anonymous", "")) {
            ftp.setFileType(FTP.BINARY_FILE_TYPE)
            ftp.enterLocalPassiveMode
            val out = new FileOutputStream(cacheFile)
            ftp.retrieveFile(path, out)
            ftp.noop
            ftp.logout
          } else {
            Log.severe("could not login")
          }
        }
        Log.info("done.")
        cacheFile
      } finally {
        if (ftp != null && ftp.isConnected) ftp.disconnect
      }
    }
  }
}
