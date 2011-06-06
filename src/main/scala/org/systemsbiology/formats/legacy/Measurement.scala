package org.systemsbiology.formats.legacy

import java.io._
import scala.collection.JavaConversions
import org.systemsbiology.formats.common._

/*
 * ISB legacy measurement data files are of the form <name>.{lambda|ratio}.
 * Each file contains a header and a number of data rows. Rows are of the format
 * <gene name><tab><field><tab><field><tab>....
 */
case class LegacyMeasurementMatrix(conditions: Array[String], values: Array[Array[String]]) {
  def apply(geneIndex: Int, conditionIndex: Int): Double = {
    java.lang.Double.parseDouble(values(geneIndex)(conditionIndex + 1))
  }
  def vngNames: Array[String] = {
    val result = new Array[String](values.length)
    for (i <- 0 until values.length) result(i) = values(i)(0)
    result
  }
}

case class LegacyMeasurement(oligoMap: Map[String, GeneNameEntry],
                             ratios: LegacyMeasurementMatrix,
                             lambdas: LegacyMeasurementMatrix)
extends GeneExpressionMeasurement {
  def conditions = ratios.conditions
  def vngNames: Array[String] = ratios.vngNames
  def geneNames: Array[String] = {
    val vngNames = ratios.vngNames
    vngNames.map(name => {
      if (oligoMap.contains(name)) oligoMap(name).geneName else name
    }).toArray
  }
  
  def apply(geneIndex: Int, conditionIndex: Int): GeneExpressionValue = {
    GeneExpressionValue(ratios(geneIndex, conditionIndex), lambdas(geneIndex, conditionIndex))
  }
}

object LegacyMeasurementReader {

  def readMeasurement(directory: File, baseName: String,
                      oligoMap: Map[String, GeneNameEntry]): LegacyMeasurement = {
    LegacyMeasurement(oligoMap,
                      readFile(new File(directory, baseName + ".ratio")),
                      readFile(new File(directory, baseName + ".lambda")))
  }

  def readFile(file: File): LegacyMeasurementMatrix = {
    println("reading: " + file.getName)
    val input = new BufferedReader(new FileReader(file))
    try {
      var line = input.readLine
      val conditionNames = line.split("\t").tail
      LegacyMeasurementMatrix(conditionNames, readDataRows(input, conditionNames.length))
    } finally {
      input.close
    }
  }

  private def readDataRows(input: BufferedReader, numConditions: Int) = {
    var dataRows : List[Array[String]] = Nil
    var line = ""
    var currentLine = 2
    while (line != null) {
      line = input.readLine
      currentLine += 1
      if (line != null) {
        val dataRow = line.trim.split("\t")
        if (dataRow.length == (numConditions + 1)) dataRows ::= dataRow
        else {
          println("WARNING ROW ONLY CONTAINS:")
          dataRow.foreach(elem => printf("'%s'\n", elem))
        }
      }
    }
    dataRows.reverse.toArray
  }
}
