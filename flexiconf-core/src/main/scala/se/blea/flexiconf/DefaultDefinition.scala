package se.blea.flexiconf

import se.blea.flexiconf.parser._

import scala.annotation.varargs

/**
 * Defines the name, parameters, and allowed child directives for a configuration directive
 *
 * User-defined directive names may consist of any non-whitespace characters as long as they
 * do not start with '$' so that they can be identified separately from built-in directives that
 * start with '$' (e.g. \$root, \$use, \$group, \$include).
 */
private[flexiconf] case class DefaultDefinition (name: String,
                                                 params: List[Param] = List.empty,
                                                 flags: Set[DirectiveFlag] = Set.empty,
                                                 documentation: String = "",
                                                 definitions: Set[Definition] = Set.empty,
                                                 source: Option[Source] = None,
                                                 warnings: List[String] = List.empty) extends Definition {
  def id: String = {
    val arity = params.size
    val block = definitions.headOption.map(_ => "*").getOrElse("")

    name + "/" + arity + block
  }
}


object DefaultDefinition {
  def fromNode(node: Node): Option[DefaultDefinition] = node match {
    case d: DefinitionNode =>
      Some(DefaultDefinition(
        name = d.name,
        params = d.parameters.map(p => Param.fromType(p.name, p.kind)),
        flags = Set.empty,
        documentation = d.documentation,
        source = Some(d.source),
        definitions = d.nodes.map(_.flatMap(fromNode).toSet[Definition]).getOrElse(Set.empty)
      ))
    case _ => None
  }
}
