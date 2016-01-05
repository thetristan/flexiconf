package se.blea.flexiconf.javaapi

import se.blea.flexiconf.{ConfigOptions, Schema}

import scala.util.{Failure, Success}


/** Java-friendly wrapper for the Parser API */
object Parser {
  def parseConfig(configFile: String, schema: Schema, opts: ConfigOptions): Config = {
    se.blea.flexiconf.Parser.parseConfig(configFile, schema, opts) match {
      case Success(c) => new Config(c)
      case Failure(ex) => throw ex
    }
  }

  def parseSchema(schemaFile: String): Schema = {
    se.blea.flexiconf.Parser.parseSchema(schemaFile) match {
      case Success(s) => s
      case Failure(ex) => throw ex
    }
  }
}
