import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object IsbDataFormats extends Build {

  override lazy val settings = super.settings ++ buildSettings

  def buildSettings = Seq(
    organization := "org.systemsbiology",
    version := "1.0",
    //scalaVersion := "2.10.0-M6",
    scalaVersion := "2.9.2",
    //scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    javacOptions in Compile ++= Seq("-target", "6", "-source", "6")
  )

  def appSettings = assemblySettings ++ testDependencies

  lazy val root = Project("root", file(".")) aggregate(data)
  lazy val data = Project("data", file("data")) settings(testDependencies :_*)

  def testDependencies = libraryDependencies ++= Seq(
    //"org.scalatest" %% "scalatest" % "1.9-2.10.0-M6-B2" % "test",
    "org.scalatest" %% "scalatest" % "1.8" % "test",
    "junit" % "junit" % "4.10" % "test",
    "commons-net" % "commons-net" % "3.1"
  )
}
