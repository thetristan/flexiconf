package se.blea.flexiconf

import se.blea.flexiconf.parser.ConfigNode
import se.blea.flexiconf.util.ExceptionUtil


/** Object that represents missing, but valid directives in the config **/
private[flexiconf] object NullDirective {
  def apply(definition: DefaultDefinition) = {
    DefaultDirective(definition.copy(name = "unknown"))
  }
}


/** Helpers for working with directives */
private[flexiconf] object DefaultDirective {
  def directiveNotAllowed = ExceptionUtil.entityNotAllowed("Directive", "Directives", "not allowed in", "allowed")
  def argumentNotAllowed = ExceptionUtil.entityNotAllowed("Argument", "Arguments", "not defined for", "defined")

  implicit def configNode2DefaultDirective(node: ConfigNode): DefaultDirective = {
    new DefaultDirective(node.directive, node.arguments, node.children.map(configNode2DefaultDirective), Some(node.source), node.warnings)
  }
}


/** Default implementation of a directive */
private[flexiconf] case class DefaultDirective(definition: DefaultDefinition,
                                               args: List[Argument] = List.empty,
                                               directives: List[DefaultDirective] = List.empty,
                                               source: Option[Source] = None,
                                               private val _warnings: List[String] = List.empty) extends Directive {
  import se.blea.flexiconf.DefaultDirective._

  // Private

  private def allowedDirectives = definition.children.map(_.name)
  private def allowedArguments = definition.parameters.map(_.name).toSet

  // Public

  // Directive implementation

  override def name: String = definition.name

  override def contains(name: String): Boolean = directives.exists(_.name == name)
  override def containsArg(name: String): Boolean = args.exists(_.name == name)

  override def allows(name: String): Boolean = allowedDirectives.contains(name)
  override def allowsArg(name: String): Boolean = allowedArguments.contains(name)

  override def apply: ArgumentValue = argValue(0)

  override def argValue(idx: Int): ArgumentValue = {
    args.lift(idx).map(_.value).getOrElse(NullValue)
  }

  override def argValue(name: String): ArgumentValue = {
    if (allowedArguments.contains(name)) {
      args.find(_.name == name).map(_.value).getOrElse(NullValue)
    } else {
      throw argumentNotAllowed(this.name, allowedArguments, Set(name))
    }
  }

  override def directive(name: String): Directive = {
    if (allowedDirectives.contains(name)) {
      directives.find(_.name == name) getOrElse NullDirective(definition.children.find(_.name == name).get)
    } else {
      throw directiveNotAllowed(this.name, allowedDirectives, Set(name))
    }
  }

  override def directives(names: String*): List[Directive] = {
    val missing = names.toSet &~ allowedDirectives
    if (missing.size == 0) {
      names.flatMap(name => directives.filter(_.name == name)).toList
    } else {
      throw directiveNotAllowed(this.name, allowedDirectives, missing)
    }
  }

  override def warnings: List[String] = _warnings ++ directives.flatMap(_.warnings)
}
