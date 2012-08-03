package org.systemsbiology.eutils

import scala.xml._
import scala.collection.mutable.{ArrayBuffer, HashMap}
import java.io.{InputStreamReader, BufferedReader, File, FileReader}
import java.util.regex.Pattern

/*
 * A set of simple wrapper objects that take advantage of the
 * fact that Scala supports XML natively
 */
// Database strings for GEO
object GEO {
  val Profiles = "geoprofiles"

  // An esearch in GDS results in an id list
  val DataSets = "gds"
}

// Wrapper for the einfo service
object EInfo {
  val BaseURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/einfo.fcgi"
  def get(db:Option[String]=None) = {
    XML.load(new java.net.URL(BaseURL) +
             db.map{ str =>
               "?db=" + str
             }.mkString)
  }
}

// Wrapper for the esearch service
object ESearch {
  val BaseURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi"
  def get(db: String, query: String, retmax: Int=1000) = {
    val url = new java.net.URL(
      BaseURL +
      "?db=%s&term=%s&retmax=%d&usehistory=y".format(db, query, retmax))
    println("URL: " + url)
    XML.load(url)
  }
}

// Wrapper for the esummary service
object ESummary {
  val BaseURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi"
  def get(db: String, ids: Seq[String]) = {
    XML.load(new java.net.URL(
      BaseURL + "?db=%s&id=%s".format(db, ids.mkString(","))))
  }
  def getFromPreviousSearch(db: String, webEnv: String, queryKey: String) = {
    val url = new java.net.URL(
      BaseURL + "?db=%s&WebEnv=%s&query_key=%s".format(db, webEnv, queryKey))
    println("URL: " + url)
    XML.load(url)
  }
}

// Wrapper for the elink service, which converts Entrez-IDs in databases
object ELink {
  val BaseURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi"
  def get(srcdb: String, destdb: String, ids: Seq[String]) = {
    XML.load(new java.net.URL(
      BaseURL + "?dbfrom=%s&db=%s&id=%s".format(srcdb,
                                                destdb,
                                                ids.mkString(","))))
  }
}

// Wrapper for the efetch service
object EFetch {
  val BaseURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi"
  def get(db: String, ids: Seq[String], webEnv: String,
            queryKey: String) = {
    val url = new java.net.URL(
      BaseURL + "?db=%s&ids=%s&WebEnv=%s&query_key=%s".format(
        db, ids.mkString(","), webEnv, queryKey))
    println("Fetching " + url)
    val in = new BufferedReader(new InputStreamReader(url.openStream))
    val buffer = new StringBuilder
    var line = in.readLine
    while (line != null) {
      buffer.append(line)
      buffer.append("\n")
      line = in.readLine
    }
    in.close
    buffer.toString
  }
}

// Helper object to provide
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

object Main extends App {

  def getSummaries(query: String) = {
    val searchresult = ESearch.get(GEO.DataSets, query)
    val webEnv = (searchresult \ "WebEnv").text
    val queryKey = (searchresult \ "QueryKey").text
    //val ids = (searchresult \ "IdList" \ "Id").map(node => node.text)
    ESummary.getFromPreviousSearch(GEO.DataSets,
                                   webEnv, queryKey)
  }

  def getSampleAccessions(organism: String) = {
    val query = "%s+AND+cel[suppFile]".format(organism)
    val summary = getSummaries(query)
    (summary \\ "Item").filter {
      item => (item \ "@Name").text == "Accession"
    }.map { item => item.text }
  }

  /**
   * Returns the unique platform identifiers available for this
   * organism.
   */
  def getPlatforms(organism: String) = {
    val query = "%s".format(organism)
    val summary = getSummaries(organism)
    (summary \\ "Item").filter {
      item => (item \ "@Name").text == "GPL"
    }.map { item => item.text.split(";") }.flatten.toSet.toSeq
  }
  
  //println(getPlatforms("synechococcus").map(a => GEOFTPURLBuilder.urlSOFTByPlatform(a)))
  //println(getSampleAccessions("synechococcus").map(a => GEOFTPURLBuilder.urlBySample(a)))
  val matrix = SOFTReader.read(
    new File("/home/weiju/Downloads/GPL7448_family.soft"))
  printf("ORF\t%s\n", matrix.sampleNames.mkString("\t"))
  for (row <- 0 until matrix.numRows) {
    print(matrix.rowNames(row))
    for (col <- 0 until matrix.numColumns) {
      printf("\t%f", matrix.values(row)(col))
    }
    printf("\n")
  }
}
