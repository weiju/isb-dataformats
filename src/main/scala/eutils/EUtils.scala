package org.systemsbiology.services.eutils

import scala.xml._
import scala.collection.mutable.{ArrayBuffer, HashMap}
import java.io.{InputStreamReader, BufferedReader, File, FileReader}
import java.util.regex.Pattern

import org.systemsbiology.formats.microarray.soft._
import org.systemsbiology.services.rsat._

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
  /*
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
  */
  val in = new BufferedReader(new FileReader("/home/weiju/Projects/ISB/isb-dataformats/src/test/resources/rsat/Halobacterium_sp_feature_names"))
  val rsatReader = new RSATSynonymReader(in)
  in.close
}
