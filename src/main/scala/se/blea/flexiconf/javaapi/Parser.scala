package se.blea.flexiconf.javaapi

import se.blea.flexiconf.config.ConfigOptions
import se.blea.flexiconf.schema.SchemaOptions
import se.blea.flexiconf.schema


/** Java-friendly wrapper for the Parser API */
object Parser {
  def parseConfig(opts: ConfigOptions): Config = {
    se.blea.flexiconf.Parser.parseConfig(opts)
      .map(new Config(_))
      .orNull
  }

  def parseSchema(opts: SchemaOptions): schema.Schema = {
    se.blea.flexiconf.Parser.parseSchema(opts).orNull
  }
}
