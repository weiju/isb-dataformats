package org.systemsbiology.formats.microarray.soft

import java.io.{BufferedReader, File, FileReader}
import scala.collection.mutable.{ArrayBuffer, HashMap}
import java.util.regex.Pattern

case class Platform(name: String, ids: Seq[String],
                    id2RowMap: Map[String, Int],
                    orfMap: Map[String, String])
case class Measurement(idref: String, value: String)
case class SampleData(name: String, values: Seq[Measurement])
case class DataMatrix(rowNames: Seq[String],
                      sampleNames: Seq[String],
                      values: Array[Array[Double]]) {
  def numRows = rowNames.length
  def numColumns = sampleNames.length
}

// Helper object for building FTP URLs
object GEOFTPURLBuilder {
  val BaseURL = "ftp://ftp.ncbi.nlm.nih.gov/pub/geo/DATA"
  val GSMPattern = Pattern.compile("GSM\\d{3,}?")
  def download {}

  // Create an FTP url for a sample accession
  def urlBySample(accession: String) = {
    val matcher = GSMPattern.matcher(accession)
    if (matcher.matches) {
      val dir = accession.replaceFirst("\\d\\d\\d$", "nnn")
      val filename = "%s.CEL.gz".format(accession)
      List(BaseURL, "supplementary", "samples", dir, accession,
           filename).mkString("/")
    } else throw new IllegalArgumentException("accession '%s' does not match GSM format".format(accession))
  }

  def urlSOFTByPlatform(platform: String) = {
    val gpl = "GPL" + platform
    List(BaseURL, "SOFT", "by_platform", gpl,
         gpl + "_family.soft.gz").mkString("/")
  }
}

object SOFTReader {
  def readPlatform(platformLine: String, in: BufferedReader) = {
    val orfMap = new HashMap[String, String]
    val id2RowMap = new HashMap[String, Int]
    val ids = new ArrayBuffer[String]
    val platformName = platformLine.split("=")(1).trim
    val values = new ArrayBuffer[Seq[String]]
    var line = in.readLine
    var row = 0

    while (!line.startsWith("!platform_table_begin")) line = in.readLine
    line = in.readLine // read the header
    val header = line.split("\t").toSeq
    val idCol = header.indexOf("ID")
    val orfCol = header.indexOf("ORF")
    line = in.readLine

    while (!line.startsWith("!platform_table_end")) {
      val comps = line.split("\t")
      ids += comps(idCol)
      orfMap(comps(idCol)) = comps(orfCol)
      id2RowMap(comps(idCol)) = row

      line = in.readLine
      row += 1
    }
    Platform(platformName, ids, id2RowMap.toMap, orfMap.toMap)
  }

  def readSample(sampleLine: String, in: BufferedReader) = {
    val sampleName = sampleLine.split("=")(1).trim
    val values = new ArrayBuffer[Measurement]

    var line = in.readLine
    while (!line.startsWith("!sample_table_begin")) line = in.readLine
    //print("parsing sample table...")
    line = in.readLine // read the header
    val header = line.split("\t").toSeq
    val idRefCol = header.indexOf("ID_REF")
    val valueCol = header.indexOf("VALUE")

    line = in.readLine
    while (!line.startsWith("!sample_table_end")) {
      val comps = line.split("\t")
      values += Measurement(comps(idRefCol), comps(valueCol))
      line = in.readLine
    }
    SampleData(sampleName, values)
  }
  def read(file: File) = {
    var in: BufferedReader = null
    val platforms = new ArrayBuffer[Platform]
    val samples = new ArrayBuffer[SampleData]
    try {
      in = new BufferedReader(new FileReader(file))
      var line = in.readLine
      while (line != null) {
        if (line.startsWith("^PLATFORM")) {
          platforms += readPlatform(line, in)
        }
        if (line.startsWith("^SAMPLE")) {
          samples += readSample(line, in)
        }
        line = in.readLine
      }
    } finally {
      if (in != null) in.close
    }
    val platform = platforms(0)
    printf("Merging platform '%s' into an array of %dx%d\n",
           platform.name, platform.ids.length, samples.length)
    val array = Array.ofDim[Double](platform.ids.length,
                                    samples.length)
    for (col <- 0 until samples.length) {
      samples(col).values.foreach { value =>
        val row = platform.id2RowMap(value.idref)
        try {
          array(row)(col) = value.value.toDouble
        } catch {
          case _ => array(row)(col) = java.lang.Double.NaN
        }
      }
    }
    DataMatrix(platform.ids.map{id => platform.orfMap(id) },
               samples.map(_.name),
               array)
  }
}
