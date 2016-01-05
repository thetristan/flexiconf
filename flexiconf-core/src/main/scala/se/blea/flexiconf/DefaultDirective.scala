package se.blea.flexiconf

import se.blea.flexiconf.parser._
import se.blea.flexiconf.util.ExceptionUtil
import se.blea.flexiconf.util.ExceptionUtil._

import scala.collection.immutable.{ListMap, TreeMap}

/** Object that represents missing directives in the config **/
private[flexiconf] object NullDirective {
  def apply(definition: Definition): DefaultDirective = {
    DefaultDirective(DefaultDefinition(name = "unknown",
      params = definition.params,
      flags = definition.flags,
      documentation = definition.documentation,
      definitions = definition.definitions,
      source = definition.source,
      warnings = definition.warnings))
  }
}

/** Helpers for working with directives */
private[flexiconf] object DefaultDirective {
  def fromNode(node: Node, definitions: Set[Definition]): Option[DefaultDirective] = node match {
    case DirectiveNode(source, name, args, nodes) =>
      val arguments = args.map(a => Arg.fromType(a.value, a.kind))

      definitions.find(matchesDefinition(name, arguments, nodes.nonEmpty, _)) map {
        definition =>
          DefaultDirective(
            definition = definition,
            arguments = arguments,
            directives = nodes.map(n => n.flatMap(fromNode(_, definition.definitions))).getOrElse(Nil),
            source = Some(source)
          )
      }
    case _ => None
  }

  private def matchesDefinition(name: String, args: List[Arg], hasBlock: Boolean, definition: Definition): Boolean = {
    val parameterTypes = definition.params map (_.kind)

    val matchesArgsLen = args.size == parameterTypes.size
    val acceptsArgsTypes = parameterTypes.zip(args).foldLeft(true) {
      case (matches, (p, a)) => matches && p.accepts(a.value)
    }

    val matchesName = name == definition.name
    val matchesArgs = matchesArgsLen && acceptsArgsTypes
    val matchesBlock = definition.definitions.nonEmpty && hasBlock || definition.definitions.isEmpty && !hasBlock

    matchesName && matchesArgs && matchesBlock
  }

  def directiveNotAllowed: IllegalStateExceptionGenerator = ExceptionUtil.entityNotAllowed("Directive", "Directives", "not allowed in", "allowed")
  def argumentNotAllowed: IllegalStateExceptionGenerator = ExceptionUtil.entityNotAllowed("Argument", "Arguments", "not defined for", "defined")
}

/** Default implementation of a directive */
private[flexiconf] case class DefaultDirective(definition: Definition,
                                               arguments: List[Arg] = List.empty,
                                               directives: List[DefaultDirective] = List.empty,
                                               source: Option[Source] = None,
                                               warnings: List[String] = List.empty) extends Directive {
  import se.blea.flexiconf.DefaultDirective._

  // Private

  private def allowedDirectives = definition.definitions.map(_.name)
  private def allowedArguments = definition.params.map(_.name).toSet


  // Public

  // Directive implementation

  override def name: String = definition.name

  override def contains(name: String): Boolean = directives.exists(_.name == name)
  override def containsArg(name: String): Boolean = args.contains(name)

  override def allows(name: String): Boolean = allowedDirectives.contains(name)
  override def allowsArg(name: String): Boolean = allowedArguments.contains(name)

  override def apply: ArgValue = argValue(0)

  override def args: ListMap[String, Arg] = ListMap(definition.params.map(_.name).zip(arguments):_*)

  override def argValue(idx: Int): ArgValue = arguments.lift(idx).map(_.argValue).getOrElse(NullValue)

  override def argValue(name: String): ArgValue = {
    if (allowedArguments.contains(name)) {
      args.get(name).map(_.argValue).getOrElse(NullValue)
    } else {
      throw argumentNotAllowed(this.name, allowedArguments, Set(name))
    }
  }

  override def directive(name: String): Directive = {
    if (allowedDirectives.contains(name)) {
      directives.find(_.name == name) getOrElse NullDirective(definition.definitions.find(_.name == name).get)
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
}
