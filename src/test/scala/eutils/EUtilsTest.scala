package org.systemsbiology.services.eutils

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EUtilsSpec extends FlatSpec with ShouldMatchers {
  "EInfo" should "return information" in {
    val result = EInfo.get(Some(GEO.Profiles))
    (result \\ "DbName").text should be ("geoprofiles")
  }
  "ESearch" should "deliver a result for synechococcus" in {
    val searchresult = ESearch.get(GEO.DataSets, "synechococcus")
    val ids = (searchresult \ "IdList" \ "Id").map(node => node.text)
    ids.length should be > (0)
    (searchresult \ "WebEnv").text.length should be > (0)
    (searchresult \ "QueryKey").text.length should be > (0)
  }
}
