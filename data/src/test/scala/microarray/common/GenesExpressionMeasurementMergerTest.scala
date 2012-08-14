package org.systemsbiology.formats.microarray.common

/**
 * Test cases for MutableGeneExpressionMeasurementMerger.
 */
import org.scalatest.{FlatSpec,BeforeAndAfterEach}
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import java.io._

@RunWith(classOf[JUnitRunner])
class GeneExpressionMeasurementMergerSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  val measurement1 = new MutableGeneExpressionMeasurement(Array("VNG0001", "VNG0002"),
                                                          Array("gene001", "gene002"),
                                                          Array("cond1", "cond2"))

  val measurement2 = new MutableGeneExpressionMeasurement(Array("VNG0001", "VNG0002"),
                                                          Array("gene001", "gene002"),
                                                          Array("cond3"))

  val measurement3 = new MutableGeneExpressionMeasurement(Array("VNG0001", "VNG0003"),
                                                          Array("gene001", "gene003"),
                                                          Array("cond4"))

  override def beforeEach {
    measurement1(0, 0) = GeneExpressionValue(0.0, 0.0)
    measurement1(0, 1) = GeneExpressionValue(0.0, 1.0)
    measurement1(1, 0) = GeneExpressionValue(1.0, 0.0)
    measurement1(1, 1) = GeneExpressionValue(1.0, 1.0)

    measurement2(0, 0) = GeneExpressionValue(0.0, 2.0)
    measurement2(1, 0) = GeneExpressionValue(1.0, 2.0)

    measurement3(0, 0) = GeneExpressionValue(0.0, 3.0)
    measurement3(1, 0) = GeneExpressionValue(1.0, 3.0)
  }

  "GeneExpressionMeasurementMerger" should "merge two measurements" in {
    val merger = new GeneExpressionMeasurementMerger(List(measurement1, measurement2))
    val merged = merger.mergedMeasurements
    merged.vngNames.length   should equal (2)
    merged.geneNames.length  should equal (2)
    merged.conditions.length should equal (3)
    merged.conditions(0) should equal ("cond1")
    merged.conditions(1) should equal ("cond2")
    merged.conditions(2) should equal ("cond3")

    merged(0, 0) should equal (GeneExpressionValue(0.0, 0.0))
  }

  it should "throw an exception on non-matching VNG lists" in {
    println("step1")
    val merger = new GeneExpressionMeasurementMerger(List(measurement1, measurement3))
    evaluating {
      println("step2")
      merger.mergedMeasurements } should produce[MeasurementMergeException]
  }
}
