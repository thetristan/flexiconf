package se.blea.flexiconf

import java.io.{FileNotFoundException, File, InputStream}

import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream}
import org.apache.commons.io.FileUtils
import se.blea.flexiconf.parser._
import se.blea.flexiconf.parser.gen.{ConfigLexer, ConfigParser, SchemaLexer, SchemaParser}

import scala.util.{Failure, Success, Try}


object Parser {
  private[flexiconf] def antlrConfigParserFromStream(inputStream: InputStream) = {
    val input = new ANTLRInputStream(inputStream)
    val lexer = new ConfigLexer(input)
    val tokens = new CommonTokenStream(lexer)

    new ConfigParser(tokens)
  }

  private[flexiconf] def antlrSchemaParserFromStream(inputStream: InputStream) = {
    val input = new ANTLRInputStream(inputStream)
    val lexer = new SchemaLexer(input)
    val tokens = new CommonTokenStream(lexer)

    new SchemaParser(tokens)
  }

  private[flexiconf] def streamFromSourceFile(sourceFile: String): Try[InputStream] = {
    Try(FileUtils.openInputStream(new File(sourceFile)))
  }

  /** Parses and returns a config with the provided options **/
  def parseConfig(configFile: String, inputStream: InputStream, schema: Schema, options: ConfigOptions): Try[Config] = {
    Try {
      val parser = antlrConfigParserFromStream(inputStream)
      val configTree = ConfigVisitor(configFile, options).visitDocument(parser.document)
      DefaultConfig.fromNode(configTree, schema)
    }
  }

  /** Parses and returns a config with the default options and provided schema **/
  def parseConfig(configFile: String, schema: Schema, options: ConfigOptions): Try[Config] = {
    streamFromSourceFile(configFile) flatMap { inputStream =>
      parseConfig(configFile, inputStream, schema, options)
    }
  }

  /** Parses and returns a schema with the provided options **/
  def parseSchema(schemaFile: String, inputStream: InputStream): Try[Schema] = {
    Try {
      val parser = antlrSchemaParserFromStream(inputStream)
      val schemaTree = SchemaVisitor(schemaFile).visitDocument(parser.document)
      DefaultSchema.fromNode(schemaTree)
    }
  }

  /** Parses and returns a schema with the default options **/
  def parseSchema(schemaFile: String): Try[Schema] = {
    streamFromSourceFile(schemaFile) flatMap { inputStream =>
      parseSchema(schemaFile, inputStream)
    }
  }

  /** Parses and returns a config with the default options after parsing the schema with the default options **/
  def parse(configFile: String, schemaFile: String, options: ConfigOptions = ConfigOptions()): Try[Config] = {
    parseSchema(schemaFile) flatMap { schema =>
      parseConfig(configFile, schema, options)
    }
  }
}
