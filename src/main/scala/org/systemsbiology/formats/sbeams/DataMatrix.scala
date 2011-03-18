package org.systemsbiology.formats.sbeams

import java.io._
import scala.collection.JavaConversions

/**
 * A DataMatrix is simply a representation of a matrix_output file, which is microarray
 * information containing lambdas and log10 ratios obtained through SBEAMS.
 */
case class DataMatrix(headers: Array[String],
                      val geneNames: Array[String],
                      data: Array[Array[String]]) {
  val conditions = new Array[String](numConditions)
  for (i <- 0 until numConditions) conditions(i) = headers(i)

  def numConditions = headers.length / 2
  def numDataRows = data.length
  def lambdaFor(row: Int, conditionIndex: Int): Double = {
    // lambdas are in the second portion of the matrix
    java.lang.Double.parseDouble(data(row)(numConditions + conditionIndex))
  }
  def ratioFor(row: Int, conditionIndex: Int): Double = {
    // ratios are in the first portion of the matrix
    java.lang.Double.parseDouble(data(row)(conditionIndex))
  }
}

object DataMatrixReader {
  def createFromFile(file: File): DataMatrix = {
    createFromFile(file, Nil, Nil)
  }
  def createFromFile(file: File,
                     conditionNames: List[String],
                     genes: List[String]): DataMatrix = {
    val input = new BufferedReader(new FileReader(file))
    try {
      skipLine(input) // this line only says RATIOS LAMBDAS
      val headerLine = readHeaderLine(input)

      val columnIndexes = makeColumnIndexesForConditionNames(headerLine,
                                                             conditionNames)
      val geneNamesAndData = readDataLines(input, columnIndexes, genes)
      DataMatrix(filteredHeader(headerLine, columnIndexes),
                 geneNamesAndData._1, geneNamesAndData._2)
    } finally {
      input.close
    }
  }

  private def filteredHeader(originalHeader: Array[String], columnIndexes: List[Int]) = {
    if (columnIndexes.length ==  0) originalHeader
    else {
      columnIndexes.map(index => originalHeader(index)).toArray
    }
  }
  private def makeColumnIndexesForConditionNames(originalHeaders: Array[String],
                                                 conditionNames: List[String]) = {
    val indexes = conditionNames.map(name => originalHeaders.indexOf(name))
    val numOriginalConditions = originalHeaders.length / 2
    indexes ++ indexes.map(_ + numOriginalConditions)
  }
  private def skipLine(reader: BufferedReader) = reader.readLine
  private def readHeaderLine(reader: BufferedReader) = {
    val headerLine = reader.readLine.split("\t")
    headerLine.drop(2).take(headerLine.length - 3)
  }
  private def readDataLines(reader: BufferedReader,
                            columnIndexes: List[Int],
                            genes: List[String]): (Array[String], Array[Array[String]]) = {
    var line = reader.readLine
    var geneNames: List[String] = Nil
    var data: List[Array[String]] = Nil
    while (line != null) {
      if (!line.startsWith("NumSigGenes:")) {
        val dataRow = line.split("\t")
        if (genes.length == 0 || genes.contains(dataRow(0))) {
          geneNames ::= dataRow(0)
          data ::= processDataRow(dataRow.drop(2).take(dataRow.length - 3), columnIndexes)
        }
      }
      line = reader.readLine
    }
    (geneNames.reverse.toArray, data.reverse.toArray)
  }
  private def processDataRow(dataRow : Array[String],
                             columnIndexes: List[Int]): Array[String] = {
    if (columnIndexes.length == 0) dataRow
    else columnIndexes.map(index => dataRow(index)).toArray
  }
}
