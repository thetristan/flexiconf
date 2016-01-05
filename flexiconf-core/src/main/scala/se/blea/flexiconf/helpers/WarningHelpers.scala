package se.blea.flexiconf.helpers

import se.blea.flexiconf.{Schema, Definition, Directive, Config}

/**
 * Created by tblease on 8/14/15.
 */
object WarningHelpers {
  implicit class ConfigWithWarnings(val c: Config) extends AnyVal {
    def collectWarnings: List[String] = c.warnings ++ c.directives.flatMap(_.warnings)
  }

  implicit class SchemaWithWarnings(val s: Schema) extends AnyVal {
    def collectWarnings: List[String] = s.warnings ++ s.definitions.flatMap(_.warnings)
  }

  implicit class DefinitionWithWarnings(val d: Definition) extends AnyVal {
    def collectWarnings: List[String] = d.warnings ++ d.definitions.flatMap(_.warnings)
  }

  implicit class DirectiveWithWarnings(val d: Directive) extends AnyVal {
    def collectWarnings: List[String] = d.warnings ++ d.directives.flatMap(_.warnings)
  }
}
