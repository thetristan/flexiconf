package se.blea.flexiconf.javaapi

import se.blea.flexiconf.directive

import scala.collection.JavaConversions._


/** Java-friendly wrapper for the ConfigNode API */
class Directive(private val _node: directive.Directive) {
  def getName: String = _node.name
  def getArgs: java.util.List[Argument] = _node.args.map(new Argument(_))
  def getChildren: java.util.List[Directive] = _node.children.map(new Directive(_))

  def getBoolArg(name: String): Boolean = _node.boolArg(name)
  def getPercentageArg(name: String): Double = _node.percentageArg(name)
  def getDecimalArg(name: String): Double = _node.decimalArg(name)
  def getStringArg(name: String): String = _node.stringArg(name)
  def getIntArg(name: String): Long = _node.intArg(name)
  def getDurationArg(name: String): Long = _node.durationArg(name)
}
