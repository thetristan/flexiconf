package se.blea.flexiconf.javaapi

import se.blea.flexiconf

import scala.collection.JavaConversions._


/** Java-friendly wrapper for the Config API */
class Config(private val _config: flexiconf.config.Config) {
  def getDirectives: java.util.List[Directive] = _config.directives.map(new Directive(_))
  def getWarnings: java.util.List[String] = _config.warnings
  def renderTree = _config.renderTree
}
