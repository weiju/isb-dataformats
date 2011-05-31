import sbt._

class IsbProject(info: ProjectInfo) extends DefaultProject(info) {
  val junit4 = "junit" % "junit" % "4.8.2" % "test"
  val scalatest = "org.scalatest" % "scalatest_2.9.0" % "1.4.1" % "test"
}

