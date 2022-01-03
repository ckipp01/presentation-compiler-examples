import mill._, scalalib._

object `scala-2-presentation-compiler-examples` extends ScalaModule {
  def scalaVersion = "2.13.7"
  def semanticDbVersion = "4.4.30"
  def ivyDeps = Agg(
    ivy"com.lihaoyi::pprint:0.7.1",
    ivy"${scalaOrganization()}:scala-compiler:${scalaVersion()}"
  )
  def scalacOptions = Seq("-Wunused")
}

object `scala-3-presentation-compiler-examples` extends ScalaModule {
  def scalaVersion = "3.1.0"
  def semanticDbVersion = "4.4.30"
  def ivyDeps = Agg(
    ivy"org.scala-lang::scala3-compiler:3.1.0",
    ivy"io.get-coursier:interface:1.0.6",
    ivy"com.lihaoyi::pprint:0.7.1"
  )
}
