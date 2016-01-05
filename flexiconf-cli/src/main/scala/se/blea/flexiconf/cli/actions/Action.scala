package se.blea.flexiconf.cli.actions

import se.blea.flexiconf.cli.CLI
import se.blea.flexiconf._

import scala.util.{Failure, Success, Try}

/**
 * Created by tblease on 5/23/15.
 */
trait Action {
  import Parser._

  def apply(args: List[String]): Unit
  def documentation: String
  def name: String
  def usage: String

  def parseWithWarnings(configPath: String, schemaPath: String, fn: Config => Unit): Unit = {
    val config = parseSchema(schemaPath) flatMap { schema =>
      streamFromSourceFile(configPath) flatMap { inputStream =>
        val configOpts = ConfigOptions(
          allowUnknownDirectives = true,
          allowDuplicateDirectives = true,
          allowMissingGroups = true,
          allowMissingIncludes = true,
          allowIncludeCycles = true
        )

        parseConfig(configPath, inputStream, schema, configOpts)
      }
    }

    config match {
      case Success(c) => fn(c)
      case Failure(ex) => CLI.err(ex.toString + "\n  " + ex.getStackTrace.mkString("\n  "))
    }
  }

  def exitWithUsageError(): Unit = {
    CLI.exit(s"action $name requires args: $usage", 1)
  }
}
