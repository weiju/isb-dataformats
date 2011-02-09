package org.systemsbiology.sbeams

/**
 * Test cases for DataMatrixReader
 */
import org.specs._
import org.specs.runner.{ConsoleRunner, JUnit4}

import java.io._

class DataMatrixTest extends JUnit4(DataMatrixSpec)
object DataMatrixSpecRunner extends ConsoleRunner(DataMatrixSpec)

object DataMatrixSpec extends Specification {

  var matrixOutputFile: File = null

  "DataMatrixReader" should {
    doBefore {
      matrixOutputFile = new File(getClass.getResource("/matrix_output").getFile)
    }
    "create a DataMatrix" in {
      val matrix = DataMatrixReader.createFromFile(matrixOutputFile)
      matrix must notBeNull
      matrix.headers.length must_== 21
      matrix.numDataRows must_== 2400
      matrix.numConditions must_== 9
      matrix.conditions(0) must_== "CU_-5_vs_NRC-1.sig"
      matrix.geneNames.length must_== 2400
      matrix.geneNames(0) must_== "HO04N09"
      matrix.geneNames(matrix.geneNames.length - 1) must_!= "NumSigGenes:"
      matrix.ratioFor(0, 0) must beCloseTo(0.026, 0.0001)
      matrix.lambdaFor(0, 0) must beCloseTo(2.103, 0.0001)
    }
  }
}
