package org.systemsbiology.formats.legacy

import java.io._
import scala.collection.JavaConversions
import org.systemsbiology.formats.common._

/*
 * ISB legacy measurement data files are of the form <name>.{lambda|ratio}.
 * Each file contains a header and a number of data rows. Rows are of the format
 * <gene name><tab><field><tab><field><tab>....
 */
case class MeasurementMatrix(conditions: Array[String], values: Array[Array[String]]) {
  def apply(geneIndex: Int, conditionIndex: Int): Double = {
    java.lang.Double.parseDouble(values(geneIndex)(conditionIndex + 1))
  }
  def vngNames: Array[String] = {
    val result = new Array[String](values.length)
    for (i <- 0 until values.length) result(i) = values(i)(0)
    result
  }
}

case class Measurement(ratios: MeasurementMatrix, lambdas: MeasurementMatrix)
extends GeneExpressionMeasurement {
  def conditions = ratios.conditions
  def vngNames = ratios.vngNames
  
  def apply(geneIndex: Int, conditionIndex: Int): GeneExpressionValue = {
    GeneExpressionValue(ratios(geneIndex, conditionIndex), lambdas(geneIndex, conditionIndex))
  }
}

object MeasurementReader {

  def readMeasurement(directory: File, baseName: String): Measurement = {
    Measurement(readFile(new File(directory, baseName + ".ratio")),
                readFile(new File(directory, baseName + ".lambda")))
  }

  def readFile(file: File): MeasurementMatrix = {
    val input = new BufferedReader(new FileReader(file))
    try {
      var line = input.readLine
      val conditionNames = line.split("\t").tail
      MeasurementMatrix(conditionNames, readDataRows(input))
    } finally {
      input.close
    }
  }

  private def readDataRows(input: BufferedReader) = {
    var dataRows : List[Array[String]] = Nil
    var line = ""
    while (line != null) {
      line = input.readLine
      if (line != null) {
        dataRows ::= line.split("\t")
      }
    }
    dataRows.reverse.toArray
  }
}
