package org.systemsbiology.formats.sbeams

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
      matrixOutputFile = new File(getClass.getResource("/sbeams/matrix_output").getFile)
    }
    "create a DataMatrix" in {
      val matrix = DataMatrixReader.createFromFile(matrixOutputFile)
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
    "create a DataMatrix with selected genes" in {
      val matrix = DataMatrixReader.createFromFile(matrixOutputFile, Nil,
                                                   List("HO04E04", "HO06E22", "HO01G10"))
      matrix.headers.length must_== 21
      matrix.numDataRows must_== 3
      matrix.numConditions must_== 9
      matrix.geneNames.length must_== 3
      matrix.geneNames(0) must_== "HO04E04"
      matrix.geneNames(1) must_== "HO06E22"
      matrix.geneNames(2) must_== "HO01G10"
      matrix.ratioFor(0, 0) must beCloseTo(-0.082, 0.0001)
      matrix.lambdaFor(0, 0) must beCloseTo(23.430, 0.0001)
    }
/*
    "create a DataMatrix with selected conditions" in {
      val matrix = DataMatrixReader.createFromFile(matrixOutputFile,
                                                   Nil,
                                                   Nil)
      matrix.headers.length must_== 21
      matrix.numDataRows must_== 3
      matrix.numConditions must_== 9
      matrix.geneNames.length must_== 3
      matrix.geneNames(0) must_== "HO04E04"
      matrix.geneNames(1) must_== "HO06E22"
      matrix.geneNames(2) must_== "HO01G10"
      matrix.ratioFor(0, 0) must beCloseTo(-0.082, 0.0001)
      matrix.lambdaFor(0, 0) must beCloseTo(23.430, 0.0001)
    }*/
  }
}
