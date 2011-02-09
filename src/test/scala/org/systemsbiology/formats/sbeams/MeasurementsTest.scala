package org.systemsbiology.formats.sbeams

/**
 * Test cases for the Measurement class.
 */
import org.specs._
import org.specs.runner.{ConsoleRunner, JUnit4}

import java.io._

class MeasurementTest extends JUnit4(MeasurementSpec)
object MeasurementSpecRunner extends ConsoleRunner(MeasurementSpec)

object MeasurementSpec extends Specification {
  var oligoMap: Map[String, String]     = null
  var dataMatrix: DataMatrix = null

  "Measurement" should {
    doBefore {
      dataMatrix = DataMatrixReader.createFromFile(new File(getClass.getResource("/sbeams/matrix_output").getFile))
      oligoMap = new OligoMapDatabase(new File(getClass.getResource("/sbeams/Slide_Templates").getFile)).latestMap
    }
    "create a Measurement" in {
      val measurement = new Measurement(oligoMap, dataMatrix)
      measurement.conditions must_== dataMatrix.conditions
      measurement.geneNames(0) must_== dataMatrix.geneNames(0)
      measurement(0, 0).ratio must_== dataMatrix.ratioFor(0, 0)
      measurement(0, 0).lambda must_== dataMatrix.lambdaFor(0, 0)
      measurement.vngNames.length must_== dataMatrix.geneNames.length
      measurement.vngNames(0) must_== "VNG1951G"
    }
  }
}
