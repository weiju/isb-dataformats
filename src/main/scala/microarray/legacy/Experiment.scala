package org.systemsbiology.formats.microarray.legacy

import java.io._
import scala.xml._

/**
 * Experiment objects.
 */
case class Experiment(name: String, date: String,
                      predicates: List[ExperimentPredicate],
                      links: List[ExperimentLink],
                      conditions: List[ExperimentCondition])
case class ExperimentPredicate(category: String, value: String)
case class ExperimentLink(linkType: String, url: String)
case class ExperimentCondition(alias: String, variables: List[ConditionVariable])
case class ConditionVariable(name: String, value: String, units: Option[String])

/**
 * Parse experiments that were 
 */
class ExperimentParser(xmlfile: File) {
  val experimentDoc = XML.loadFile(xmlfile)

  def makeExperiment = {
    val experiment = new Experiment((experimentDoc \ "@name").text,
                                    (experimentDoc \ "@date").text,
                                    makePredicates, makeLinks,
                                    makeConditions)
    experiment
  }

  private def makePredicates = {
    var predicates: List[ExperimentPredicate] = Nil
    for (predicate <- (experimentDoc \ "predicate")) {
      predicates ::= ExperimentPredicate((predicate \ "@category").text,
                                         (predicate \ "@value").text)
    }
    predicates.reverse
  }
  private def makeLinks = {
    var links: List[ExperimentLink] = Nil
    for (link <- (experimentDoc \ "link")) {
      links ::= ExperimentLink((link \ "@type").text,
                               (link \ "@url").text)
    }
    links.reverse
  }
  private def makeConditions = {
    var conditions: List[ExperimentCondition] = Nil
    for (condition <- (experimentDoc \ "condition")) {
      conditions ::= ExperimentCondition((condition \ "@alias").text,
                                         makeVariables(condition))
    }
    conditions
  }

  private def makeVariables(condition: NodeSeq) = {
    var variables: List[ConditionVariable] = Nil
    for (variable <- (condition \ "variable")) {
      val units = variable \ "@units"
      variables ::= ConditionVariable((variable \ "@name").text,
                                      (variable \ "@value").text,
                                      if (!units.isEmpty) Some(units.text) else None)
    }
    variables
  }
}

/**
 * A class to load all XML-file based experiments in a directory.
 * 
 */
class ExperimentDirectory(workDirectory: File) {
  private val experiments: List[Experiment] = loadExperiments
  
  private def loadExperiments: List[Experiment] = {
    if (workDirectory.exists && workDirectory.isDirectory) {
      val xmlFiles = workDirectory.listFiles.filter(file => file.getName.endsWith(".xml"))
      // read the xml files
      var experiments : List[Experiment] = Nil
      for (xmlfile <- xmlFiles) {
        experiments ::= new ExperimentParser(xmlfile).makeExperiment
      }
      experiments
    } else {
      throw new IllegalArgumentException("%s is not a directory.".format(workDirectory))
    }
  }

  def numExperiments: Int = experiments.length
  def experimentNames = experiments.map(e => e.name)
  def experiment(name: String): Option[Experiment] = experiments.find(e => name == e.name)
}
