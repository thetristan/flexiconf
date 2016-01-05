package se.blea.flexiconf.cli.actions

import se.blea.flexiconf.cli.CLI

import scala.util.{Failure, Success}

/** Print a representation of a configuration tree */
object InspectSchemaAction extends Action {
  import se.blea.flexiconf.Parser._

  override def name: String = "inspectSchema"
  override def usage: String = "<schemaPath>"
  override def documentation: String = "Parse schema, print schema tree and warnings to stdout"

  override def apply(args: List[String]): Unit = {
    if (args.length != 1) {
      exitWithUsageError()
    }

    val schemaPath = args(0)
    parseSchema(schemaPath) match {

      case Success(schema) =>
        import se.blea.flexiconf.helpers.RenderHelpers._

        CLI.out(schema.renderTree())

        if (schema.warnings.nonEmpty) {
          CLI.err("Warnings:")
          schema.warnings.foreach(w => CLI.err(s"- $w"))
        }

      case Failure(e) =>
        CLI.err("Couldn't parse schema: \n- " + e.getMessage)
    }
  }
}
