package org.systemsbiology.formats.sbeams

/**
 * Test cases for DataMatrixReader
 */
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import java.io._

@RunWith(classOf[JUnitRunner])
class DataMatrixSpec extends FlatSpec with ShouldMatchers {

  val matrixOutputFile = new File(getClass.getResource("/sbeams/matrix_output").getFile)

  "A DataMatrixReader" should "create a DataMatrix" in {
    val matrix = DataMatrixReader.createFromFile(matrixOutputFile)
    matrix.headers.length   should equal (21)
    matrix.numDataRows      should equal (2400)
    matrix.numConditions    should equal (9)
    matrix.conditions(0)    should equal ("CU_-5_vs_NRC-1.sig")
    matrix.geneNames.length should equal (2400)
    matrix.geneNames(0)     should equal ("HO04N09")
    matrix.geneNames(matrix.geneNames.length - 1) should not equal ("NumSigGenes:")
    matrix.ratioFor(0, 0)   should be (0.026 plusOrMinus 0.0001)
    matrix.lambdaFor(0, 0)  should be (2.103 plusOrMinus 0.0001)
  }
  it should "create a DataMatrix with selected genes" in {
    val matrix = DataMatrixReader.createFromFile(matrixOutputFile, Nil,
                                                 List("HO04E04", "HO06E22", "HO01G10"))
    matrix.headers.length   should equal (21)
    matrix.numDataRows      should equal (3)
    matrix.numConditions    should equal (9)
    matrix.geneNames.length should equal (3)
    matrix.geneNames(0)     should equal ("HO04E04")
    matrix.geneNames(1)     should equal ("HO06E22")
    matrix.geneNames(2)     should equal ("HO01G10")
    matrix.ratioFor(0, 0)   should be (-0.082 plusOrMinus 0.0001)
    matrix.lambdaFor(0, 0)  should be (23.430 plusOrMinus 0.0001)
  }
}
