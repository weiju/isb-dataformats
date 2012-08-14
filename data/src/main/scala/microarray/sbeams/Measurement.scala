package org.systemsbiology.formats.microarray.sbeams

import scala.collection.JavaConversions._
import org.systemsbiology.formats.microarray.common._

/**
 * Measurement is a convenience wrapper around an oligo map and a data matrix
 * to provide easy access to measurement values.
 */
class SbeamsMeasurement(oligoMap: Map[String, GeneNameEntry], dataMatrix: DataMatrix)
extends GeneExpressionMeasurement {

  def conditions: Array[String] = dataMatrix.conditions

  def vngNames: Array[String] = {
    val orig = dataMatrix.geneNames
    orig.map(name => {
      if (oligoMap.contains(name)) oligoMap(name).vngName else name
    }).toArray
  }
  def geneNames: Array[String] = {
    val orig = dataMatrix.geneNames
    orig.map(name => {
      if (oligoMap.contains(name)) oligoMap(name).geneName else name
    }).toArray
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
