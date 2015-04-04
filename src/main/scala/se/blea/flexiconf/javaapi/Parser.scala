package se.blea.flexiconf.javaapi

import scala.collection.JavaConversions._

import se.blea.flexiconf._

/** Java-friendly wrapper for the Parser API */
object Parser {
  def parseConfig(opts: ConfigOptions): Config = {
    se.blea.flexiconf.Parser.parseConfig(opts)
      .map(new Config(_))
      .orNull
  }

  def parseSchema(opts: SchemaOptions): Schema = {
    se.blea.flexiconf.Parser.parseSchema(opts).orNull
  }
}

/** Java-friendly wrapper for the Config API */
class Config(private val _config: se.blea.flexiconf.Config) {
  def getDirectives: java.util.List[Directive] = _config.directives.map(new Directive(_))
  def getWarnings: java.util.List[String] = _config.warnings
  def renderTree = _config.renderTree
}

/** Java-friendly wrapper for the ConfigNode API */
class Directive(private val _node: se.blea.flexiconf.Directive) {
  def getName: String = _node.name
  def getArgs: java.util.List[Argument] = _node.args.map(new Argument(_))
  def getChildren: java.util.List[Directive] = _node.children.map(new Directive(_))

  def getBoolArg(name: String): Boolean = _node.boolArg(name)
  def getPercentageArg(name: String): Double = _node.percentageArg(name)
  def getDecimalArg(name: String): Double = _node.decimalArg(name)
  def getStringArg(name: String): String = _node.stringArg(name)
  def getIntArg(name: String): Long = _node.intArg(name)
  def getDurationArg(name: String): Long = _node.durationArg(name)
}

/** Java-friendly wrapper for the Argument API */
class Argument(private val _arg: se.blea.flexiconf.Argument) {
  def getName = _arg.name
  def getKind = _arg.kind match {
    case StringArgument => ArgumentKind.String
    case IntArgument => ArgumentKind.Int
    case BoolArgument => ArgumentKind.Bool
    case DecimalArgument => ArgumentKind.Decimal
    case DurationArgument => ArgumentKind.Duration
    case PercentageArgument => ArgumentKind.Percentage
    case _ => throw new IllegalArgumentException("Unknown argument kind")
  }
}
