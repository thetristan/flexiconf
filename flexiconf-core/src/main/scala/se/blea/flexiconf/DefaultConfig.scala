package se.blea.flexiconf

import se.blea.flexiconf.parser.ConfigNode


/** Container for a configuration tree */
private[flexiconf] case class DefaultConfig(private val config: ConfigNode) extends Config {
  import DefaultDirective._

  // Private

  private lazy val collapsedConfig = config.collapse
  private lazy val allowedDirectives = collapsedConfig.directive.children.map(_.name)

  // Public

  // Config implementation

  override def renderTree: String = collapsedConfig.children.map(_.renderTree()).mkString("")
  override private[flexiconf] def renderDebugTree = config.children.map(_.renderTree()).mkString("")

  // TraversableConfig implementation

  override def contains(name: String): Boolean = directives.exists(_.name == name)

  override def allows(name: String): Boolean = allowedDirectives.contains(name)

  override def directive(name: String): Directive = {
    if (allowedDirectives.contains(name)) {
      directives.find(_.name == name) getOrElse NullDirective(config.directive.children.find(_.name == name).get)
    } else {
      throw directiveNotAllowed("top-level of config", allowedDirectives, Set(name))
    }
  }

  override lazy val directives: List[DefaultDirective] = collapsedConfig.children.map(configNode2DefaultDirective)

  override def directives(names: String*): List[Directive] = {
    val missing = names.toSet &~ allowedDirectives
    if (missing.size == 0) {
      names.flatMap(name => directives.filter(_.name == name)).toList
    } else {
      throw directiveNotAllowed("top-level of config", allowedDirectives, missing)
    }
  }

  override def warnings: List[String] = directives.flatMap(_.warnings)
}
