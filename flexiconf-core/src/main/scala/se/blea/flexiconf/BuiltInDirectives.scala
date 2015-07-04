package se.blea.flexiconf

private[flexiconf] object BuiltInDirectives {
  /** Indicates the start of the configuration tree */
  def root(ds: Set[DirectiveDefinition] = Set.empty): DirectiveDefinition = DirectiveDefinition(name = "$root", children = ds)
  def root(ds: DirectiveDefinition*): DirectiveDefinition = root(ds.toSet)

  /** Allows inclusion of multiple, additional configuration trees */
  def include(ds: Set[DirectiveDefinition]):
  DirectiveDefinition = DirectiveDefinition(name = "$include", children = ds, parameters = List(Parameter("pattern")))

  /** Defines a group of directives that can be used elsewhere in the configuration tree */
  def group: DirectiveDefinition =
    DirectiveDefinition(name = "$group", parameters = List(Parameter("name")))

  /** Includes directives from a pre-defined group in the configuration tree */
  def use(ds: Set[DirectiveDefinition]): DirectiveDefinition =
    DirectiveDefinition( name = "$use", children = ds, parameters = List(Parameter("pattern")))

  /** Placeholder for errors encountered when parsing a configuration tree */
  def warning: DirectiveDefinition = DirectiveDefinition(name = "$warning", parameters = List(Parameter("message")))

  /** Placeholder for unknown directives when reading a configuration tree */
  val unknown = DirectiveDefinition(name = "unknown")
}
