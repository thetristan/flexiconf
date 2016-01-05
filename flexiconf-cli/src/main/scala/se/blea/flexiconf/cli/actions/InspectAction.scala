package se.blea.flexiconf.cli.actions

import se.blea.flexiconf.cli.CLI

/** Print a representation of a configuration tree */
object InspectAction extends Action {
  override def name: String = "inspect"
  override def usage: String = "<configPath> <schemaPath>"
  override def documentation: String = "Parse config, print configuration tree and warnings to stdout"

  override def apply(args: List[String]): Unit = {
    args match {
      case configPath :: schemaPath :: Nil =>
        parseWithWarnings(configPath, schemaPath, { config =>
          import se.blea.flexiconf.helpers.RenderHelpers._

          CLI.out(config.renderTree())

          if (config.warnings.nonEmpty) {
            CLI.err("Warnings:")
            config.warnings.foreach(w => CLI.err(s"- $w"))
          }
        })

      case _ => exitWithUsageError()
    }
  }
}
