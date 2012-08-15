package org.systemsbiology.geoimport

import java.io.{File, BufferedReader, InputStreamReader, FileInputStream, FileReader,
                BufferedWriter, FileWriter, PrintWriter}
import java.util.zip._
import java.util.logging._

import scala.collection.mutable.{ArrayBuffer, HashSet, HashMap}

import org.systemsbiology.services.eutils._
import org.systemsbiology.services.rsat._
import org.systemsbiology.formats.microarray.soft._

case class ImportConfig(name: String, query: String, idColumns: Seq[String])

object GeoImport extends App {
  val Log = Logger.getLogger("GeoImport")

  private def getSummaries(query: String) = {
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
  private def getPlatforms(organism: String) = {
    val query = "%s".format(organism)
    val summary = getSummaries(organism)
    (summary \\ "Item").filter {
      item => (item \ "@Name").text == "GPL"
    }.map { item => item.text.split(";") }.flatten.toSet.toSeq
  }

  private def mergeMatrices(matrices: Seq[DataMatrix]) = {
    Log.info("# matrices collected: %d".format(matrices.length))
    val allGenes = new HashSet[String]
    val allConditions = new ArrayBuffer[String]
    val gene2RowMaps = new ArrayBuffer[Map[String, Int]]

    matrices.foreach { matrix =>
      val gene2Row = new HashMap[String, Int]
      allGenes ++= matrix.rowNames
      allConditions ++= matrix.sampleNames
      matrix.rowNames.foreach { row =>
        if (gene2Row.contains(row)) {
          Log.warning("gene '%s' is redundant -> only one row will be used".format(row))
        }
        gene2Row(row) = matrix.rowNames.indexOf(row)
      }
      gene2RowMaps += gene2Row.toMap
    }

    Log.info("# genes: %d # conditions: %d\n".format(allGenes.size, allConditions.size))
    val sortedGenes = allGenes.toSeq.sortWith((s1, s2) => s1 < s2)
    val mergedValues = Array.ofDim[Double](sortedGenes.length, allConditions.length)

    var colOffset = 0
    for (i <- 0 until matrices.length) {
      val matrix = matrices(i)
      val gene2RowMap = gene2RowMaps(i)
      for (j <- 0 until sortedGenes.length) {
        if (gene2RowMap.contains(sortedGenes(j))) {
          val sourceRow = gene2RowMap(sortedGenes(j))
          for (k <- 0 until matrix.sampleNames.length) {
            mergedValues(j)(colOffset + k) = matrix.values(sourceRow)(k)
          }
        } else {
          for (k <- 0 until matrix.sampleNames.length) {
            mergedValues(j)(colOffset + k) = java.lang.Double.NaN
          }
        }
      }
      colOffset += matrix.sampleNames.length
    }
    DataMatrix(sortedGenes, allConditions, mergedValues)
  }

  def mergeOrganism(config: ImportConfig) = {
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
    var out : PrintWriter = null
    try {
      out = new PrintWriter(new BufferedWriter(new FileWriter("%s_merged.csv".format(config.name))))
      val resultMatrix = mergeMatrices(matrices)
      resultMatrix.write(out)
    } finally {      
      if (out == null) out.close
    } 
  }
  val configs = List(ImportConfig("synf", "synechococcus+elongatus+7942", List("7942_ID", "ORF")))
  configs.foreach { config => mergeOrganism(config) }
}
