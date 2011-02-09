package org.systemsbiology.sbeams

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
      oligomapDir = new File(getClass.getResource("/Slide_Templates").getFile)
    }
    "return only map files" in {
      new OligoMapDatabase(oligomapDir).numMapFiles must_== 4
    }
    "return the latest oligo file" in {
      new OligoMapDatabase(oligomapDir).latest.getName must_== "halo_oligo_13171-13200.map"
    }
    "finds a test resource file" in {
      new File(getClass.getResource("/Slide_Templates/halo_oligo_13171-13200.map").getFile).exists must beTrue
    }
    "get the latest oligo map" in {
      val latestMap = new OligoMapDatabase(oligomapDir).latestMap
      latestMap("HO01H19") must_== "VNG0475C"
    }
  }
}
