package se.blea.flexiconf.javaapi

import java.lang.{Boolean => JBoolean, Double => JDouble, Long => JLong, String => JString}
import java.util.{List => JList}

import scala.collection.JavaConversions._

/** Java-friendly wrapper for the ConfigNode API */
class Directive(private val _directive: se.blea.flexiconf.Directive) {
  def getName: JString = _directive.name

  def getArgs: JList[Argument] = _directive.args.map { case (name, arg) => new Argument(name, arg) }.toList

  def getDirectives: JList[Directive] = _directive.directives.map(new Directive(_))

  def contains(name: String): JBoolean = _directive.contains(name)

  @annotation.varargs
  def getDirectives(names: String*): JList[Directive] = _directive.directives(names:_*).map(new Directive(_))

  def getDirective(name: String): Directive = new Directive(_directive.directive(name))

  private def boolArg(name: String): Option[Boolean] = _directive.argValue(name).boolValue
  private def longArg(name: String): Option[Long] = _directive.argValue(name).longValue
  private def doubleArg(name: String): Option[Double] = _directive.argValue(name).doubleValue
  private def stringArg(name: String): Option[String] = _directive.argValue(name).stringValue

  def hasArg(name: String): JBoolean = _directive.allowsArg(name)

  def getBoolArg(name: String): JBoolean = boolArg(name).getOrElse[Boolean](false)
  def getBoolArg(name: String, default: JBoolean): JBoolean = boolArg(name).getOrElse[Boolean](default)

  def getPercentageArg(name: String): JDouble = doubleArg(name).getOrElse[Double](0.0)
  def getPercentageArg(name: String, default: JDouble): JDouble = doubleArg(name).getOrElse[Double](default)

  def getDecimalArg(name: String): JDouble = doubleArg(name).getOrElse[Double](0.0)
  def getDecimalArg(name: String, default: JDouble): JDouble = doubleArg(name).getOrElse[Double](default)

  def getStringArg(name: String): JString = stringArg(name).getOrElse[String]("")
  def getStringArg(name: String, default: JString): JString = stringArg(name).getOrElse[String](default)

  def getIntArg(name: String): JLong = longArg(name).getOrElse[Long](0L)
  def getIntArg(name: String, default: JLong): JLong = longArg(name).getOrElse[Long](default)

  def getDurationArg(name: String): JLong = longArg(name).getOrElse[Long](0L)
  def getDurationArg(name: String, default: JLong): JLong = longArg(name).getOrElse[Long](default)
}
