import $dep.`org.scala-lang:scala-compiler:2.13.6`

import scala.reflect.internal.util.ScriptSourceFile
import scala.reflect.io.VirtualDirectory
import scala.tools.nsc.interactive.Global
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.Settings

val ourScalaCode = """|package example
                      |
                      |object Main extends App {
                      |  def greeting(word: String): Unit = {
                      |    println(word)
                      |  }
                      |
                      |  greeting("hi")
                      |}
                      |""".stripMargin

val vd: VirtualDirectory = new VirtualDirectory("(memory)", None)

val settings = new Settings

settings.usejavacp.value = true

val reporter: ConsoleReporter = new ConsoleReporter(settings)

val compiler = new Global(settings, reporter, "presentation-compiler-example")
import compiler._

val unit = newCompilationUnit(ourScalaCode)

val tree = compiler.parseTree(unit.source)

// This should be right in the wo<<cursor>>rd parameter in greeting
val pos = unit.position(60)

// Our fake little cache that we'll "cache" the trees that we visited
var lastVisitedParentTrees: List[Tree] = Nil

compiler.onUnitOf(pos.source) { unit =>
  new MetalsLocator(pos).locateIn(tree)
}

// A copy pasta of the locator we actually use in Metals
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
                  // to the annotation on the symbol of the annotatee.
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
lastVisitedParentTrees.map { tree =>
  (tree.pos.start, tree.pos.end)
}.distinct
