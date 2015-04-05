package se.blea.flexiconf.schema

import se.blea.flexiconf.directive.{DirectiveDefinition, DirectiveFlag}
import se.blea.flexiconf.parameter.Parameter
import se.blea.flexiconf.util.Source


/** Public interface to the schema API */
case class Schema(private val rawSchema: DefaultSchemaNode) extends SchemaNode {
  private lazy val schema = rawSchema.collapse

  override def name: String = schema.name
  override def children: List[SchemaNode] = schema.children
  override def parameters: List[Parameter] = schema.parameters
  override def source: Source = schema.source
  override def documentation: String = schema.documentation
  override def flags: Set[DirectiveFlag] = schema.flags
  override def toDirectives: Set[DirectiveDefinition] = schema.toDirectives
}




