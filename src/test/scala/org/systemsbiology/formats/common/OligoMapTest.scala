package org.systemsbiology.formats.common

/**
 * Test cases for OligoMapDatabase
 */
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import java.io._

@RunWith(classOf[JUnitRunner])
class OligoMapDatabaseSpec extends FlatSpec with ShouldMatchers {
  // point to our dummy map directory
  val oligomapDir = new File(getClass.getResource("/sbeams/Slide_Templates").getFile)
  "OligoMapDatabase" should "return only map files" in {
    new OligoMapDatabase(oligomapDir).numMapFiles should equal (4)
  }
  it should "return the latest oligo file" in {
    new OligoMapDatabase(oligomapDir).latest.getName should equal ("halo_oligo_13171-13200.map")
  }
  it should "get the latest oligo map" in {
    val latestMap = new OligoMapDatabase(oligomapDir).latestMap
    latestMap("HO04N09") should equal (GeneNameEntry("VNG1951G", "sub"))
  }
  it should "get the latest mapping from VNG to gene names" in {
    val latestVngMap = new OligoMapDatabase(oligomapDir).latestVng2GeneNameMap
    latestVngMap("VNG1951G") should be ("sub")
  }
}
