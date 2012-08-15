package org.systemsbiology.geoimport

import java.io.{File, BufferedReader, InputStreamReader, FileInputStream, FileReader}
import java.util.zip._
import java.util.logging._

import scala.collection.mutable.{ArrayBuffer, HashSet}

import org.systemsbiology.services.eutils._
import org.systemsbiology.services.rsat._
import org.systemsbiology.formats.microarray.soft._

case class ImportConfig(query: String, idColumns: Seq[String])

object GeoImport extends App {
  val Log = Logger.getLogger("GeoImport")

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
    val synonyms = new RSATSynonymReader(new BufferedReader(
      new FileReader("/home/weiju/Projects/ISB/isb-dataformats/synf_feature_names.tab"))).synonyms
    urls.foreach { url =>
      val file = SOFTReader.download(url, new File("cache"))
      var gzip: BufferedReader = null
      try {
        gzip = new BufferedReader(
          new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))
        val matrix = SOFTReader.read(gzip, config.idColumns)
        if (matrix != null) matrices += matrix
      } catch {
        case e:Throwable =>
          Log.log(Level.SEVERE,
                  "error in processing - skipping file '%s'".format(file.getName), e)
      } finally {
        if (gzip != null) gzip.close
      }
    }
    Log.info("# matrices collected: %d".format(matrices.length))
    val allGenes = new HashSet[String]
    val allConditions = new ArrayBuffer[String]
/*
    matrices.foreach { matrix =>
      //allGenes ++= matrix.rowNames
      //allConditions ++= matrix.sampleNames
    }
    */
    matrices(0).rowNames.foreach { row =>
      if (allGenes.contains(row)) {
        Log.warning("GENE '%s' is ALREADY IN !!!!".format(row))
      }
      allGenes += row
    }
    Log.info("# genes: %d # conditions: %d\n".format(allGenes.size, allConditions.size))
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
  val configs = List(ImportConfig("synechococcus+elongatus+7942", List("7942_ID", "ORF")))
  configs.foreach { config => mergeOrganism(config) }
}
