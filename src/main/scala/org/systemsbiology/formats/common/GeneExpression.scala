package org.systemsbiology.formats.common

/**
 * A measurement value, grouping ratio and lambda for a specific position in an
 * SBEAMS generated gene expression measurement.
 */
case class GeneExpressionValue(ratio: Double, lambda: Double)

/**
 * Interface for gene expression measurements.
 */
trait GeneExpressionMeasurement {

  /**
   * Returns this measurement's condition names.
   * @return condition names
   */
  def conditions: Array[String]

  /**
   * Returns this measurement's VNG gene names.
   * @return VNG gene names
   */
  def vngNames: Array[String]

  /**
   * Returns this measurement's real common gene names if possible.
   * When there is no official name available, the name will fall back to the
   * VNG name
   * @return official gene names
   */
  def geneNames: Array[String]

  /**
   * Retrieve the measurement value at the specified position.
   * @param geneIndex gene name index
   * @param condition index condition name index
   * @return measurement value at the specified position
   */
  def apply(geneIndex: Int, conditionIndex: Int): GeneExpressionValue
}

/**
 * A class which can be used to combine measurements from various sources.
 * @constructor creates a MutableGeneExpressionMeasurement instance
 * @param vngNames the VNG names
 * @param "real" gene names
 * @param conditions the conditions
 */
class MutableGeneExpressionMeasurement(val vngNames: Array[String],
                                       val geneNames: Array[String],
                                       val conditions: Array[String])
extends GeneExpressionMeasurement {
  val data = Array.ofDim[GeneExpressionValue](vngNames.length, conditions.length)
  def apply(geneIndex: Int, conditionIndex: Int) = data(geneIndex)(conditionIndex)
  def update(geneIndex: Int, conditionIndex: Int, value: GeneExpressionValue) {
    data(geneIndex)(conditionIndex) = value
  }
}
