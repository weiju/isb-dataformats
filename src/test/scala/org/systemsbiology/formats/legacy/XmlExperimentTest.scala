package org.systemsbiology.formats.legacy

/**
 * Test cases for XML experiments.
 */
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import java.io._

@RunWith(classOf[JUnitRunner])
class ExperimentSpec extends FlatSpec with ShouldMatchers {

  val workDirectory = new File(getClass.getResource("/xml-experiments").getFile)

  "ExperimentDirectory" should "load experiments in a work directory" in {
    val experimentDir = new ExperimentDirectory(workDirectory)
    experimentDir.numExperiments             should be (3)
    experimentDir.experiment("Fe add-back")  should not be (None)
    experimentDir.experiment("non-existing") should be (None)
    experimentDir.experimentNames.length     should be (3)
  }
  it should  "read information contained in experiment" in {
    val experimentDir = new ExperimentDirectory(workDirectory)
    val feAddBack = experimentDir.experiment("Fe add-back").get
    feAddBack.name                                                  should be ("Fe add-back")
    feAddBack.date                                                  should be ("2003-02-10")
    feAddBack.predicates.find(p => p.category == "species")         should not be (None)
    feAddBack.predicates.find(p => p.category == "strain")          should not be (None)
    feAddBack.predicates.find(p => p.category == "perturbation")    should not be (None)
    feAddBack.links.find(link => link.linkType == "journalArticle") should not be (None)
    
    val cond = feAddBack.conditions.find(cond => cond.alias =="FeAddBack-1-0")
    cond.get.variables.find(v => v.name == "time").get.units should be (Some("minutes"))
    cond.get.variables.find(v => v.name == "time").get.value should be ("000")
  }
}
