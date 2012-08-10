package org.systemsbiology.services.rsat

import java.io.{BufferedReader}
import scala.collection.mutable.HashMap


class RSATSynonymReader(reader: BufferedReader) {
  val synonyms = readSynonyms

  private[this] def readSynonyms: Map[String, String] = {
    val result = new HashMap[String, String]
    var line = reader.readLine
    while (line != null) {
      if (!line.startsWith("--")) {
        val comps = line.split("\t")
        result(comps(1)) = comps(0)
      }
      line = reader.readLine
    }
    result.toMap
  }
}
