package se.blea.flexiconf.schema

import se.blea.flexiconf.directive.{DirectiveDefinition, DirectiveFlag}
import se.blea.flexiconf.parameter.Parameter
import se.blea.flexiconf.util.Source

/** Base trait for nodes representing schema data */
trait SchemaNode {
  def name:String
  def parameters: List[Parameter]
  def source: Source
  def flags: Set[DirectiveFlag]
  def documentation: String
  def children: List[SchemaNode]
  def toDirectives: Set[DirectiveDefinition]
}
