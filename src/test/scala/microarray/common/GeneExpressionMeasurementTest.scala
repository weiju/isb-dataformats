package org.systemsbiology.formats.microarray.common

/**
 * Test cases for MutableGeneExpressionMeasurement.
 */
import org.scalatest.{FlatSpec,BeforeAndAfterEach}
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import java.io._

@RunWith(classOf[JUnitRunner])
class GeneExpressionMeasurementSpec extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {
  var measurement : MutableGeneExpressionMeasurement = null

  override def beforeEach {
    measurement = new MutableGeneExpressionMeasurement(Array("VNG0001", "VNG0002"),
                                                       Array("gene1", "gene2"),
                                                       Array("condition1", "condition2", "condition3"))
  }

  "MutableGeneExpressionMeasurement" should "have its values initialized with null" in {
    measurement(0, 0) should be (null)
  }
  it should "be possible to change the values" in {
    val value = GeneExpressionValue(1.0, 2.0)
    measurement(0, 0) = value
    measurement(0, 0) should be (value)
  }
}
