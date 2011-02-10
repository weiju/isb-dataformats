package org.systemsbiology.formats.legacy

/**
 * Test cases for the legacy measurement reader.
 */
import org.specs._
import org.specs.runner.{ConsoleRunner, JUnit4}

import java.io._

class MeasurementReaderTest extends JUnit4(MeasurementReaderSpec)
object MeasurementReaderSpecRunner extends ConsoleRunner(MeasurementReaderSpec)

object MeasurementReaderSpec extends Specification {

  var ratioFile: File = null
  var baseDirectory: File = null

  "ExperimentDirectory" should {
    doBefore {
      ratioFile = new File(getClass.getResource("/legacy/zinc.ratio").getFile)
      baseDirectory = new File(getClass.getResource("/legacy").getFile)
    }
    "read a ratio file" in {
      val matrix = MeasurementReader.readFile(ratioFile)
      matrix.conditions.length must_== 3
      matrix.conditions(0) must_== "zn__0005um_vs_NRC-1"
      matrix.values.length must_== 2400
      matrix.vngNames.length must_== 2400
      matrix.values(0)(0) must_== "VNG6413H"
      matrix.values(0)(1) must_== "0.013"
    }
    "read a measurement" in {
      val measurement = MeasurementReader.readMeasurement(baseDirectory, "zinc")
      measurement.conditions.length must_== 3
      measurement.conditions(0) must_== "zn__0005um_vs_NRC-1"
      measurement.vngNames.length must_== 2400
      measurement(0, 0).lambda must beCloseTo(0.081, 0.0001)
      measurement(0, 0).ratio must beCloseTo(0.013, 0.0001)
    }
  }
}