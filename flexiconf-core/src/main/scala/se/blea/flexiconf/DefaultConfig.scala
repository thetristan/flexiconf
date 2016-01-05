package se.blea.flexiconf

import se.blea.flexiconf.parser._


/** Container for a configuration tree */
case class DefaultConfig(directives: List[Directive],
                         private val allowedDirectives: Set[Definition]) extends Config {

  private lazy val allowedDirectivesByName = allowedDirectives.map(dd => dd.name -> dd).toMap

  // TraversableConfig implementation

  override def contains(name: String): Boolean = directives.exists(_.name == name)

  override def allows(name: String): Boolean = allowedDirectivesByName.contains(name)

  override def directive(name: String): Directive = {
    import se.blea.flexiconf.DefaultDirective._

    allowedDirectivesByName.get(name) map { dd =>
      directives.find(_.name == name) getOrElse NullDirective(dd)
    } getOrElse {
      throw directiveNotAllowed("top-level of config", allowedDirectivesByName.keySet, Set(name))
    }
  }

  override def directives(names: String*): List[Directive] = {
    import se.blea.flexiconf.DefaultDirective._

    val missing = names.toSet &~ allowedDirectivesByName.keySet
    if (missing.isEmpty) {
      names.flatMap(name => directives.filter(_.name == name)).toList
    } else {
      throw directiveNotAllowed("top-level of config", allowedDirectivesByName.keySet, missing)
    }
  }

  override def warnings: List[String] = directives.flatMap(_.warnings)
}

object DefaultConfig {
  def fromNode(node: Node, schema: Schema): DefaultConfig = {
    val directives: List[Directive] = node match {
      case TreeNode(t) => t.nodes.map(n => n.flatMap(DefaultDirective.fromNode(_, schema.definitions))).getOrElse(Nil)
      case _ => Nil
    }

    DefaultConfig(directives, schema.definitions)
  }
}
