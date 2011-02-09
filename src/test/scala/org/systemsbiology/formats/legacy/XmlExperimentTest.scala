package org.systemsbiology.formats.legacy

/**
 * Test cases for XML experiments.
 */
import org.specs._
import org.specs.runner.{ConsoleRunner, JUnit4}

import java.io._

class ExperimentTest extends JUnit4(ExperimentSpec)
object ExperimentSpecRunner extends ConsoleRunner(ExperimentSpec)

object ExperimentSpec extends Specification {

  var workDirectory: File = null

  "ExperimentDirectory" should {
    doBefore {
      workDirectory = new File(getClass.getResource("/xml-experiments").getFile)
    }
    "load experiments in a work directory" in {
      val experimentDir = new ExperimentDirectory(workDirectory)
      experimentDir.numExperiments must_== 3
      experimentDir.experiment("Fe add-back") must_!= None
      experimentDir.experiment("non-existing") must_== None
      experimentDir.experimentNames.length must_== 3
    }
    "read information contained in experiment" in {
      val experimentDir = new ExperimentDirectory(workDirectory)
      val feAddBack = experimentDir.experiment("Fe add-back").get
      feAddBack.name must_== "Fe add-back"
      feAddBack.date must_== "2003-02-10"
      feAddBack.predicates.find(p => p.category == "species") must_!= None
      feAddBack.predicates.find(p => p.category == "strain") must_!= None
      feAddBack.predicates.find(p => p.category == "perturbation") must_!= None
      feAddBack.links.find(link => link.linkType == "journalArticle") must_!= None

      val cond = feAddBack.conditions.find(cond => cond.alias =="FeAddBack-1-0")
      cond.get.variables.find(v => v.name == "time").get.units must_== Some("minutes")
      cond.get.variables.find(v => v.name == "time").get.value must_== "000"
    }
  }
}
