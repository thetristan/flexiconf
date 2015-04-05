package se.blea.flexiconf.config

import se.blea.flexiconf.directive.Directive

/** Public API for accessing parsed configs */
trait Config {
  def directives: List[Directive]
  def warnings: List[String]
  def renderTree: String
}

