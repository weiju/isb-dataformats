package org.systemsbiology.formats.common

/**
 * A measurement value, grouping ratio and lambda for a specific position in an
 * SBEAMS generated gene expression measurement.
 */
case class GeneExpressionValue(ratio: Double, lambda: Double)

trait GeneExpressionMeasurement {
  def conditions: Array[String]
  def vngNames: Array[String]

  def apply(geneIndex: Int, conditionIndex: Int): GeneExpressionValue
}
