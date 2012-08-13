name         := "isb-dataformats"

version      := "1.0"

organization := "org.systemsbiology"

scalaVersion := "2.10.0-M6"

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers += "official Maven mirror" at "http://mirrors.ibiblio.org/pub/mirrors/maven2/"

libraryDependencies ++= Seq("org.scalatest" %% "scalatest" % "1.9-2.10.0-M6-B2",
                            "junit" % "junit" % "4.10",
                            "commons-net" % "commons-net" % "3.1")

seq(sbtassembly.Plugin.assemblySettings: _*)

