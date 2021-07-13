import scala.reflect.io.VirtualDirectory
import scala.tools.nsc.Settings
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.reporters.ConsoleReporter

object SelectionRanges extends App {
  val ourScalaCode = """|object Main extends App {
                        |  def greeting(word: String): Unit = {
                        |    println(word)
                        |  }
                        |}
                        |""".stripMargin

  val vd: VirtualDirectory = new VirtualDirectory("(memory)", None)

  val settings = new Settings

  settings.usejavacp.value = true

  val reporter: ConsoleReporter = new ConsoleReporter(settings)

  val compiler = new Global(settings, reporter, "presentation-compiler-example")
  import compiler._

  val unit = newCompilationUnit(ourScalaCode)

// If we simply wanted the full tree, this would give it to us.
  val tree = compiler.parseTree(unit.source)

  // no the i in String
  val pos = unit.position(50)

// Our fake little cache that we'll "cache" the trees that we visited
  var lastVisitedParentTrees: List[Tree] = Nil

  compiler.onUnitOf(pos.source) { _ =>
    new MetalsLocator(pos).locateIn(tree)
  }

// This is very similiar to the custom traverser that we use in Metals, just simplified a bit.
  class MetalsLocator(pos: Position) extends Traverser {
    def locateIn(root: Tree): Tree = {
      lastVisitedParentTrees = Nil
      traverse(root)
      lastVisitedParentTrees match {
        case head :: _ => head
        case _         => EmptyTree
      }
    }
    override def traverse(t: Tree): Unit = {
      t match {
        case _: TypeTree =>
          () // ignore for now since we're not dealing with a typed tree for our example
        case _ =>
          if (t.pos.includes(pos)) {
            lastVisitedParentTrees ::= t
            super.traverse(t)
          } else {
            t match {
              case mdef: MemberDef =>
                val annotationTrees = mdef.mods.annotations match {
                  case Nil if mdef.symbol != null =>
                    // After typechecking, annotations are moved from the modifiers
                    // to the annotation on the symbol of the annotationTrees
                    mdef.symbol.annotations.map(_.original)
                  case anns => anns
                }
                traverseTrees(annotationTrees)
              case _ =>
            }
          }
      }
    }
  }

// Here are the unique semantic selection ranges
  val ranges = lastVisitedParentTrees.map { tree =>
    (tree.pos.start, tree.pos.end)
  }.distinct

  ranges.zipWithIndex
    .foreach { case ((start, end), index) =>
      val selection = ourScalaCode.slice(start, end)
      pprint
        .copy(colorLiteral = fansi.Color.Blue)
        .pprintln(s"Selection Range: ${index + 1}")
      pprint.pprintln(selection)
    }
}
