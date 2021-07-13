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

object SelectionRanges:
  @main def createSelectionRanges: Unit =
    val ourScalaCode = """|object Main:
                          |  @main def hello: Unit =
                          |    println("Hello world!")
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

    given ctx: Context = driver.currentCtx

    val filename = "Example.scala"
    val uri = java.net.URI.create(s"file:///$filename")

    val sourceFile = SourceFile.virtual(filename, ourScalaCode)

    driver.run(
      uri,
      sourceFile
    )

    // If we simply wanted the full tree, this would give it to us.
    val tree = driver.currentCtx.run.units.head.untpdTree

    // 35 between H<<cursor>>ello
    val pos = new SourcePosition(sourceFile, Spans.Span(55))

    val trees =
      Interactive.pathTo(driver.openedTrees(uri), pos)

    val ranges = trees.map { tree =>
      (tree.sourcePos.start, tree.sourcePos.end)
    }.distinct

    ranges.zipWithIndex
      .foreach { case ((start, end), index) =>
        val selection = ourScalaCode.slice(start, end)
        pprint
          .copy(colorLiteral = fansi.Color.Blue)
          .pprintln(s"Selection Range: ${index + 1}")
        pprint.pprintln(selection)
      }
