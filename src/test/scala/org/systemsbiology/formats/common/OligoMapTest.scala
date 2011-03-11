package org.systemsbiology.formats.common

/**
 * Test cases for OligoMapDatabase
 */
import org.specs._
import org.specs.runner.{ConsoleRunner, JUnit4}

import java.io._

class OligoMapTest extends JUnit4(OligoMapDatabaseSpec)
object OligoMapDatabaseSpecRunner extends ConsoleRunner(OligoMapDatabaseSpec)

object OligoMapDatabaseSpec extends Specification {
  var oligomapDir: File = null
  "OligoMapDatabase" should {
    doBefore {
      // point to our dummy map directory
      oligomapDir = new File(getClass.getResource("/sbeams/Slide_Templates").getFile)
    }
    "return only map files" in {
      new OligoMapDatabase(oligomapDir).numMapFiles must_== 4
    }
    "return the latest oligo file" in {
      new OligoMapDatabase(oligomapDir).latest.getName must_== "halo_oligo_13171-13200.map"
    }
    "get the latest oligo map" in {
      val latestMap = new OligoMapDatabase(oligomapDir).latestMap
      latestMap("HO04N09") must_== GeneNameEntry("VNG1951G", "sub")
    }
  }
}
