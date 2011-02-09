package org.systemsbiology.sbeams

import java.io._
import scala.collection.JavaConversions

/**
 * A DataMatrix is simply a representation of a matrix_output file, which is microarray
 * information containing lambdas and log10 ratios obtained through SBEAMS.
 */
case class DataMatrix(headers: Array[String], data: Array[Array[String]]) {
  val conditions = new Array[String](numConditions)
  for (i <- 0 until numConditions) conditions(i) = headers(i + 2)

  def numConditions = ((headers.length - 3) / 2)
  def numDataRows = data.length
  def geneNames: Array[String] = {
    val result = new Array[String](data.length)
    for (i <- 0 until data.length) result(i) = data(i)(0)
    result
  }
  def lambdaFor(row: Int, conditionIndex: Int): Double = {
    // lambdas are in the second portion of the matrix
    java.lang.Double.parseDouble(data(row)(2 + numConditions + conditionIndex))
  }
  def ratioFor(row: Int, conditionIndex: Int): Double = {
    // ratios are in the first portion of the matrix
    java.lang.Double.parseDouble(data(row)(2 + conditionIndex))
  }
}

object DataMatrixReader {
  def createFromFile(file: File): DataMatrix = {
    val input = new BufferedReader(new FileReader(file))
    try {
      skipLine(input)
      DataMatrix(readHeaderLine(input), readDataLines(input))
    } finally {
      input.close
    }
  }
  private def skipLine(reader: BufferedReader) = reader.readLine
  private def readHeaderLine(reader: BufferedReader) = reader.readLine.split("\t")
  private def readDataLines(reader: BufferedReader): Array[Array[String]] = {
    var line = reader.readLine
    var data: List[Array[String]] = Nil
    while (line != null) {
      if (!line.startsWith("NumSigGenes:"))
        data ::= line.split("\t")

      line = reader.readLine
    }
    data.reverse.toArray
  }
}
