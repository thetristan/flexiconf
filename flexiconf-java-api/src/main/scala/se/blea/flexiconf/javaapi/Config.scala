package se.blea.flexiconf.javaapi

import java.lang.{Boolean => JBoolean, String => JString}
import java.util.{List => JList}

import scala.collection.JavaConversions._


/** Java-friendly wrapper for the Config API */
class Config(private val _config: se.blea.flexiconf.Config) {
  import se.blea.flexiconf.helpers.RenderHelpers._

  def getDirectives: JList[Directive] = _config.directives.map(new Directive(_))

  def contains(name: String): JBoolean = _config.contains(name)

  @annotation.varargs
  def getDirectives(names: String*): JList[Directive] = _config.directives
    .filter( d => names.contains(d.name) )
    .map(new Directive(_))

  def getDirective(name: String): Directive = new Directive(_config.directive(name))

  def getWarnings: JList[String] = _config.warnings

  def renderTree: JString = _config.renderTree()
}
