name         := "isb-dataformats"

version      := "1.0"

organization := "org.systemsbiology"

scalaVersion := "2.9.1"

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers += "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"

resolvers += "official Maven mirror" at "http://mirrors.ibiblio.org/pub/mirrors/maven2/"

libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "1.8",
                            "junit" % "junit" % "4.10")

seq(sbtassembly.Plugin.assemblySettings: _*)

