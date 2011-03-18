package org.systemsbiology.formats.sbeams

/**
 * Test cases for the Measurement class.
 */
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import java.io._
import org.systemsbiology.formats.common._

@RunWith(classOf[JUnitRunner])
class MeasurementSpec extends FlatSpec with ShouldMatchers {
  val dataMatrix = DataMatrixReader.createFromFile(new File(getClass.getResource("/sbeams/matrix_output").getFile))
  val oligoMap = new OligoMapDatabase(new File(getClass.getResource("/sbeams/Slide_Templates").getFile)).latestMap

  "A Measurement" should "create a Measurement" in {
    val measurement = new SbeamsMeasurement(oligoMap, dataMatrix)
    measurement.conditions should equal (dataMatrix.conditions)
    measurement.geneNames(0) should equal ("sub")
    measurement(0, 0).ratio should equal (dataMatrix.ratioFor(0, 0))
    measurement(0, 0).lambda should equal (dataMatrix.lambdaFor(0, 0))
    measurement.vngNames.length should equal (dataMatrix.geneNames.length)
    measurement.vngNames(0) should equal ("VNG1951G")
  }
}
