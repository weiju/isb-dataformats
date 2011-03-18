package org.systemsbiology.formats.legacy

/**
 * Test cases for the legacy measurement reader.
 */
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.systemsbiology.formats.common._

import java.io._

@RunWith(classOf[JUnitRunner])
class MeasurementReaderSpec extends FlatSpec with ShouldMatchers {

  val ratioFile = new File(getClass.getResource("/legacy/zinc.ratio").getFile)
  val baseDirectory = new File(getClass.getResource("/legacy").getFile)
  val oligoMap = new OligoMapDatabase(new File(getClass.getResource("/sbeams/Slide_Templates").getFile)).latestMap

  "ExperimentDirectory" should "read a ratio file" in {
    val matrix = LegacyMeasurementReader.readFile(ratioFile)
    matrix.conditions.length should be (3)
    matrix.conditions(0)     should be ("zn__0005um_vs_NRC-1")
    matrix.values.length     should be (2400)
    matrix.vngNames.length   should be (2400)
    matrix.values(0)(0)      should be ("VNG6413H")
    matrix.values(0)(1)      should be ("0.013")
  }
  it should "read a measurement" in {
    val measurement = LegacyMeasurementReader.readMeasurement(baseDirectory, "zinc", oligoMap)
    measurement.conditions.length should be (3)
    measurement.conditions(0)     should be ("zn__0005um_vs_NRC-1")
    measurement.vngNames.length   should be (2400)
    measurement(0, 0).lambda      should be (0.081 plusOrMinus 0.0001)
    measurement(0, 0).ratio       should be (0.013 plusOrMinus 0.0001)
  }
}
