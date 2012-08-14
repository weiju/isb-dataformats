package org.systemsbiology.geoimport

import java.io.{File, BufferedReader, InputStreamReader, FileInputStream, FileReader}
import java.util.zip._
import scala.collection.mutable.ArrayBuffer

import org.systemsbiology.services.eutils._
import org.systemsbiology.services.rsat._
import org.systemsbiology.formats.microarray.soft._

case class ImportConfig(query: String, idColumn: String)

object GeoImport extends App {
  def getSummaries(query: String) = {
    val searchresult = ESearch.get(GEO.DataSets, query)
    val webEnv = (searchresult \ "WebEnv").text
    val queryKey = (searchresult \ "QueryKey").text
    ESummary.getFromPreviousSearch(GEO.DataSets,
                                   webEnv, queryKey)
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

  def mergeOrganism(config: ImportConfig) {
    val urls = getPlatforms(config.query).map(a => GEOFTPURLBuilder.urlSOFTByPlatform(a))
    val matrices = new ArrayBuffer[DataMatrix]
    //val synonyms = new RSATSynonymReader(new BufferedReader(
    //  new FileReader("/home/weiju/Projects/ISB/isb-dataformats/synf_feature_names.tab"))).synonyms
    urls.foreach { url =>
      val file = SOFTReader.download(url, new File("cache"))
      var gzip: BufferedReader = null
      try {
        gzip = new BufferedReader(
          new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))
        val matrix = SOFTReader.read(gzip, config.idColumn)
        if (matrix != null) matrices += matrix
      } catch {
        case e:Throwable =>
          printf("ERROR in processing - skipping file '%s'\n", file.getName)
        e.printStackTrace
      } finally {
        if (gzip != null) gzip.close
      }
    }
    println("# MATRICES COLLECTED: " + matrices.length)
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
  val configs = List(ImportConfig("synechococcus+elongatus+7942", "7942_ID"))
  configs.foreach { config => mergeOrganism(config) }
}
