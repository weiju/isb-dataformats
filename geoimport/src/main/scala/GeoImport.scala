package org.systemsbiology.geoimport

import org.systemsbiology.services.eutils._
import org.systemsbiology.formats.microarray.soft._
import java.io.{File, BufferedReader, InputStreamReader, FileInputStream}
import java.util.zip._

object GeoImport extends App {
  def getSummaries(query: String) = {
    val searchresult = ESearch.get(GEO.DataSets, query)
    val webEnv = (searchresult \ "WebEnv").text
    val queryKey = (searchresult \ "QueryKey").text
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
  
  val query = "synechococcus+elongatus+7942"
  val urls = getPlatforms(query).map(a => GEOFTPURLBuilder.urlSOFTByPlatform(a))
  println("\n\n----------------------\nFTP URLS: ")

  urls.foreach { url =>
    val file = SOFTReader.download(url, new File("cache"))
    var gzip: BufferedReader = null
    try {
      gzip = new BufferedReader(
        new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))
      val matrix = SOFTReader.read(gzip, "7942_ID")
    } catch {
      case e:Throwable =>
        printf("ERROR in processing - skipping file '%s'\n", file.getName)
        e.printStackTrace
    } finally {
      if (gzip != null) gzip.close
    }
/*
    printf("GENE\t%s\n", matrix.sampleNames.mkString("\t"))
    for (row <- 0 until matrix.numRows) {
      print(matrix.rowNames(row))
      for (col <- 0 until matrix.numColumns) {
        printf("\t%f", matrix.values(row)(col))
      }
      printf("\n")
    }*/
  }
  //println(getSampleAccessions(query).map(a => GEOFTPURLBuilder.urlBySample(a)))
  /*
  val synonyms = new RSATSynonymReader(new BufferedReader(
    new FileReader("/home/weiju/Projects/ISB/isb-dataformats/synf_feature_names.tab"))).synonyms
    */
}
