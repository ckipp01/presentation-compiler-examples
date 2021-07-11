import $dep.`org.scala-lang::scala3-compiler:3.0.0`
import $dep.`io.get-coursier:interface:1.0.4`

import java.io.File

import dotty.tools.dotc.interactive.InteractiveDriver

import coursierapi.Dependency
import coursierapi.Fetch

import scala.jdk.CollectionConverters._

val ourScalaCode = """|@main def hello: Unit =
                      |  printline("Hello world!")
                      |""".stripMargin

val fetch = Fetch.create()

fetch.addDependencies(
  Dependency.of("org.scala-lang", "scala3-library_3", "3.0.0")
)

val extraLibraries = fetch
  .fetch()
  .asScala
  .map(_.toPath())
  .toSeq

val driver = new InteractiveDriver(
  List(
    "-color:never",
    "-classpath",
    extraLibraries.mkString(File.pathSeparator)
  )
)

// Here is where you'll find the DiagnosticError that printline is not found
driver.run(
  java.net.URI.create("file:///Example.scala"),
  ourScalaCode
)
