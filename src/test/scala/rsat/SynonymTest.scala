package org.systemsbiology.services.rsat

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.{File, FileReader, BufferedReader}

// Test for the RSAT feature names file reader
@RunWith(classOf[JUnitRunner])
class RSATSynonymReaderSpec extends FlatSpec with ShouldMatchers {
  "RSATSynonymReaderSpec" should "read a feature name file" in {
    val reader = new BufferedReader(new FileReader(
      getClass.getResource("/rsat/Halobacterium_sp_feature_names").getFile))
    val synonyms = new RSATSynonymReader(reader).synonyms
    synonyms.size should be > (0)
    synonyms("trn45") should be ("VNGt45")
  }
}
