import $dep.`org.scala-lang::scala3-compiler:3.0.0`
import $dep.`io.get-coursier:interface:1.0.4`

import java.nio.file.Path
import java.io.File

import dotty.tools.dotc.interactive.InteractiveDriver
import dotty.tools.dotc.util.SourceFile
import dotty.tools.dotc.util.SourcePosition
import dotty.tools.dotc.util.Spans
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.interactive.Interactive

import coursierapi.Dependency
import coursierapi.Fetch

import scala.jdk.CollectionConverters._

val ourScalaCode = """|@main def hello: Unit =
                      |  println("Hello world!")
                      |""".stripMargin

val fetch = Fetch.create()

fetch.addDependencies(
  Dependency.of("org.scala-lang", "scala3-library_3", "3.0.0")
)

val extraLibraries: Seq[Path] = fetch
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

given ctx: Context = driver.currentCtx

val filename = "Example.scala"
val uri = java.net.URI.create(s"file:///$filename")

val sourceFile = SourceFile.virtual(filename, ourScalaCode)

driver.run(
  uri,
  sourceFile
)

val tree = driver.currentCtx.run.units.head.untpdTree

// 35 between He<<cursor>>llo
val pos = new SourcePosition(sourceFile, Spans.Span(35))

 val trees =
   Interactive.pathTo(driver.openedTrees(uri), pos)(using ctx)

 trees.map { tree =>
   (tree.sourcePos.start, tree.sourcePos.end)
 }.distinct
