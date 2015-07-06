package se.blea.flexiconf

private[flexiconf] object BuiltInDirectives {
  /** Indicates the start of the configuration tree */
  def root(ds: Set[DefaultDefinition] = Set.empty): DefaultDefinition = DefaultDefinition(name = "$root", children = ds)
  def root(ds: DefaultDefinition*): DefaultDefinition = root(ds.toSet)

  /** Allows inclusion of multiple, additional configuration trees */
  def include(ds: Set[DefaultDefinition]):
  DefaultDefinition = DefaultDefinition(name = "$include", children = ds, parameters = List(Parameter("pattern")))

  /** Defines a group of directives that can be used elsewhere in the configuration tree */
  def group: DefaultDefinition =
    DefaultDefinition(name = "$group", parameters = List(Parameter("name")))

  /** Includes directives from a pre-defined group in the configuration tree */
  def use(ds: Set[DefaultDefinition]): DefaultDefinition =
    DefaultDefinition( name = "$use", children = ds, parameters = List(Parameter("pattern")))

  /** Placeholder for errors encountered when parsing a configuration tree */
  def warning: DefaultDefinition = DefaultDefinition(name = "$warning", parameters = List(Parameter("message")))

  /** Placeholder for unknown directives when reading a configuration tree */
  val unknown = DefaultDefinition(name = "unknown")
}
