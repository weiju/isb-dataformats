package org.systemsbiology.formats.sbeams

import org.systemsbiology.formats.common._

/**
 * Measurement is a convenience wrapper around an oligo map and a data matrix
 * to provide easy access to measurement values.
 */
class Measurement(oligoMap: Map[String, String], dataMatrix: DataMatrix)
extends GeneExpressionMeasurement {

  def conditions: Array[String] = dataMatrix.conditions
  def geneNames: Array[String]  = dataMatrix.geneNames

  def vngNames: Array[String] = {
    val orig = geneNames
    val result = new Array[String](orig.length)
    for (i <- 0 until result.length) result(i) = oligoMap(orig(i))
    result
  }
  
  /**
   * Allows to access the measurement values like in a two-dimensional
   * array, indexed by the data row and the condition index.
   */
  def apply(row: Int, conditionIndex: Int): GeneExpressionValue = {
    GeneExpressionValue(dataMatrix.ratioFor(row, conditionIndex),
                        dataMatrix.lambdaFor(row, conditionIndex))
  }
}
