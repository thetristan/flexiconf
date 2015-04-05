package se.blea.flexiconf.directive

import se.blea.flexiconf.argument.Argument


/** Public interface for consuming configuration */
trait Directive {
  def name: String
  def args: List[Argument]
  def children: List[Directive]
  def warnings: List[String]

  def intArg(name: String): Long
  def stringArg(name: String): String
  def boolArg(name: String): Boolean
  def decimalArg(name: String): Double
  def durationArg(name: String): Long
  def percentageArg(name: String): Double
}





/** Values needed to determine whether a possible directive matches an actual one */
case class MaybeDirective(private[flexiconf] val name: String,
                          private[flexiconf] val arguments: Seq[Argument] = Seq.empty,
                          private[flexiconf] val hasBlock: Boolean = false) {

  /** Returns true if the provided provided Directive matches this MaybeDirective */
  private[flexiconf] def matches(directive: DirectiveDefinition) = {
    val argumentKinds = arguments map (_.kind)
    val parameterKinds = directive.parameters map (_.kind)

    val matchesName = name == directive.name
    val matchesArgs = argumentKinds == parameterKinds
    val matchesBlock = directive.requiresBlock && hasBlock || !directive.requiresBlock && !hasBlock

    matchesName && matchesArgs && matchesBlock
  }

  /** Returns true if the provided Directive doesn't match this MaybeDirective */

  private[flexiconf] def doesNotMatch(directive: DirectiveDefinition) = {
    !matches(directive)
  }

  override def toString = {
    var res = name

    if (arguments.nonEmpty) {
      res ++= arguments.map({ a => s"<${a.value}>:${a.kind}" }).mkString(" ", " ", "")
    }

    if (hasBlock) {
      res ++= " {}"
    }

    res
  }
}




