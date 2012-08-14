package org.systemsbiology.services.eutils

import scala.xml._
import scala.collection.mutable.{ArrayBuffer, HashMap}
import java.io.{BufferedReader, InputStreamReader}
import java.util.regex.Pattern

/*
 * A set of simple wrapper objects that take advantage of the
 * fact that Scala supports XML natively
 */
// Database string constants for GEO
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

// Helper object for building FTP URLs
object GEOFTPURLBuilder {
  val BaseURL = "ftp://ftp.ncbi.nlm.nih.gov/pub/geo/DATA"
  val GSMPattern = Pattern.compile("GSM\\d{3,}?")

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
    List(BaseURL, "SOFT", "by_platform", gpl, gpl + "_family.soft.gz").mkString("/")
  }
}
