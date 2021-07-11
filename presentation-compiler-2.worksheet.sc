import $dep.`org.scala-lang:scala-compiler:2.13.6`

import scala.reflect.io.VirtualDirectory
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.Settings

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

// This should be right in the wo<<cursor>>rd parameter in greeting
val pos = unit.position(60)

// Our fake little cache that we'll "cache" the trees that we visited
var lastVisitedParentTrees: List[Tree] = Nil

compiler.onUnitOf(pos.source) { unit =>
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

assert(
  ranges == List((56, 60), (28, 86), (12, 88), (0, 88)),
  "Correct ranges not found"
)
