import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object IsbDataFormats extends Build {

  override lazy val settings = super.settings ++ buildSettings

  def buildSettings = Seq(
    organization := "org.systemsbiology",
    version := "1.0",
    scalaVersion := "2.10.0-M6",
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    javacOptions in Compile ++= Seq("-target", "6", "-source", "6")
  )

  def appSettings = assemblySettings ++ testDependencies

  lazy val root = Project("root", file(".")) aggregate(geoimport)
  lazy val geoimport = Project("geoimport",
                                file("geoimport")) settings (appSettings: _*) dependsOn(data)
  lazy val data = Project("data", file("data")) settings(testDependencies :_*)

  def testDependencies = libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "1.9-2.10.0-M6-B2" % "test",
    "junit" % "junit" % "4.10" % "test",
    "commons-net" % "commons-net" % "3.1")
}
