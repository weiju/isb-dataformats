package org.systemsbiology.formats.common

/**
 * An Exception that is thrown when a list of measurements could not be merged.
 */
class MeasurementMergeException extends Exception

/**
 * A measurment merger class. A list of GeneExpressionMeasurement instances
 * are merged, assuming they have the same list of VNG names.
 * The measurements are merged by appending the values in the order
 * the measuremenst are listed.
 * @constructor creates a merger instance
 * @param measurements the list of measurements to be merged
 */
class GeneExpressionMeasurementMerger(measurements: List[GeneExpressionMeasurement]) {

  /**
   * Returns a GeneExpressionMeasurement which represents the merge
   * of the input measurements.
   * @return merged measurment
   */
  def mergedMeasurements: GeneExpressionMeasurement = {
    val result = new MutableGeneExpressionMeasurement(mergedVngNames, mergedGeneNames, mergedConditions)
    mergeValuesInto(result)
    result
  }
  private def mergedVngNames = {
    val compVngNames = measurements.head.vngNames
    for (measurement <- measurements.tail) {
      if (!stringArraysAreEqual(compVngNames, measurement.vngNames)) {
        println("THROW EXCEPTION")
        throw new MeasurementMergeException
      }
    }
    measurements.head.vngNames
  }
  private def stringArraysAreEqual(stringArray0: Array[String],
                                   stringArray1: Array[String]): Boolean = {
    if (stringArray0.length == stringArray1.length) {
      for (i <- 0 until stringArray0.length) {
        if (stringArray0(i) != stringArray1(i)) return false
      }
      true
    } else false
  }
  private def mergedGeneNames = measurements.head.geneNames

  private def mergedConditions = {
    measurements.flatMap(measurement => measurement.conditions).toArray
  }
  private def mergeValuesInto(result: MutableGeneExpressionMeasurement) {
    for (rowIndex <- 0 until result.vngNames.length) {
      var columnIndex = 0
      for (measurement <- measurements;
           conditionIndex <- 0 until measurement.conditions.length) {
        result(rowIndex, columnIndex) = measurement(rowIndex, conditionIndex)
        columnIndex += 1
      }
    }
  }
}

