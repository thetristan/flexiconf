package se.blea.flexiconf.parser

import se.blea.flexiconf.{DefaultDefinition, DirectiveFlag, Parameter, Source}

trait Definition {
  def name:String
  def parameters: List[Parameter]
  def source: Source
  def flags: Set[DirectiveFlag]
  def documentation: String
  def children: List[Definition]
  def toDirectives: Set[DefaultDefinition]
}
