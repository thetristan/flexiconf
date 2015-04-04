package se.blea.flexiconf

import org.antlr.v4.runtime.ParserRuleContext
import se.blea.flexiconf.parser.gen.SchemaParser.{FlagAllowOnceContext, FlagContext, FlagListContext}
import se.blea.flexiconf.parser.gen.SchemaParserBaseVisitor

import scala.annotation.varargs
import scala.collection.JavaConversions._


/** Public interface for consuming configuration */
trait Directive {
  def name: String
  def args: List[Argument]
  def children: List[Directive]
  def warnings: List[String]

  def intArg(name: String): Long
  def stringArg(name: String): String
  def boolArg(name: String): Boolean
  def decimalArg(name: String): Double
  def durationArg(name: String): Long
  def percentageArg(name: String): Double
}


/** Default implementation of a directive */
class DefaultDirective(private val node: ConfigNode) extends Directive {
  /** Name of this directive */
  override def name: String = node.name

  /** Collect all warnings associated with this directive and child directives */
  override def warnings: List[String] = node.warnings

  /** Get all child directives for this directive */
  override def children: List[Directive] = node.children.map(new DefaultDirective(_))

  /** Get all args **/
  override def args: List[Argument] = node.arguments

  /** Get int value of argument **/
  override def intArg(argName: String): Long = getArg(argName, IntArgument)

  /** Get string value of argument **/
  override def stringArg(argName: String): String = getArg(argName, StringArgument)

  /** Get boolean value of argument **/
  override def boolArg(argName: String): Boolean = getArg(argName, BoolArgument)

  /** Get decimal value of argument **/
  override def decimalArg(argName: String): Double = getArg(argName, DecimalArgument)

  /** Get duration value of argument **/
  override def durationArg(argName: String): Long = getArg(argName, DurationArgument)

  /** Get percentage value of argument **/
  override def percentageArg(argName: String): Double = getArg(argName, PercentageArgument)

  /** Return the argument value if it exists, throws exception otherwise */
  private def getArg[T](argName: String, kind: ArgumentKind[T]): T = {
    // throw exception if the directive accepts no argument
    if (args.size == 0) {
      throw new IllegalStateException(s"Unknown argument: $argName - directive '$name' accepts no arguments")
    }

    // Get argument value or throw exception if the named argument isn't valid for this directive
    args.find(_.name == argName).map(_.value).map(kind.valueOf) getOrElse {
      val validArgs = node.directive.parameters.mkString(" ")
      throw new IllegalStateException(s"Unknown argument: $argName - valid arguments for directive '$name': $validArgs")
    }
  }
}


/** Values needed to determine whether a possible directive matches an actual one */
case class MaybeDirective(private[flexiconf] val name: String,
                          private[flexiconf] val arguments: Seq[Argument] = Seq.empty,
                          private[flexiconf] val hasBlock: Boolean = false) {

  /** Returns true if the provided provided Directive matches this MaybeDirective */
  private[flexiconf] def matches(directive: DirectiveDefinition) = {
    val argumentKinds = arguments map (_.kind)
    val parameterKinds = directive.parameters map (_.kind)

    val matchesName = name == directive.name
    val matchesArgs = argumentKinds == parameterKinds
    val matchesBlock = directive.requiresBlock && hasBlock || !directive.requiresBlock && !hasBlock

    matchesName && matchesArgs && matchesBlock
  }

  /** Returns true if the provided Directive doesn't match this MaybeDirective */

  private[flexiconf] def doesNotMatch(directive: DirectiveDefinition) = {
    !matches(directive)
  }

  override def toString = {
    var res = name

    if (arguments.nonEmpty) {
      res ++= arguments.map({ a => s"<${a.value}>:${a.kind}" }).mkString(" ", " ", "")
    }

    if (hasBlock) {
      res ++= " {}"
    }

    res
  }
}


trait DirectiveFlag {
}


object DirectiveFlags {
  object AllowOnce extends DirectiveFlag {
    override def toString: String = "once"
  }
}


/** Flags that affect how directives should be handled when creating the final configuration tree */
case class DirectiveFlags(flags: Set[DirectiveFlag] = Set.empty) {
  def allowOnce = flags.contains(DirectiveFlags.AllowOnce)

  override def toString: String = {
    flags.mkString("[", ",", "]")
  }
}


/** Returns DirectiveFlags based on supported flags for a directive */
private[flexiconf] object DirectiveFlagListVisitor extends SchemaParserBaseVisitor[Set[DirectiveFlag]] {
  def apply(ctx: ParserRuleContext): Set[DirectiveFlag] = ctx match {
    case flagList: FlagListContext => visitFlagList(flagList)
    case _ => Set.empty
  }

  override def visitFlagList(ctx: FlagListContext): Set[DirectiveFlag] = {
    ctx.flag.foldLeft(Set[DirectiveFlag]()) { (flags, current) =>
      DirectiveFlagVisitor(current) match {
        case Some(f) => flags + f
        case _ => flags
      }
    }
  }

  object DirectiveFlagVisitor extends SchemaParserBaseVisitor[Option[DirectiveFlag]] {
    def apply(ctx: ParserRuleContext): Option[DirectiveFlag] = ctx match {
      case flag: FlagContext => visitFlag(flag)
      case _ => None
    }

    override def visitFlagAllowOnce(ctx: FlagAllowOnceContext): Option[DirectiveFlag] = Some(DirectiveFlags.AllowOnce)
  }
}


/**
 * Defines the name, parameters, and allowed child directives for a configuration directive
 *
 * User-defined directive names must match the pattern "[a-zA-Z]\w+" so that they can be identified separately
 * from built-in directives that start with '$' (e.g. $root, $use, $group, $include).
 */
case class DirectiveDefinition private[flexiconf](private[flexiconf] val name: String,
                                     private[flexiconf] val parameters: List[Parameter] = List.empty,
                                     private[flexiconf] val flags: Set[DirectiveFlag] = Set.empty,
                                     private[flexiconf] val documentation: String = "",
                                     private[flexiconf] val children: Set[DirectiveDefinition] = Set.empty) {

  /** True if this directive expects a block */
  private[flexiconf] val requiresBlock = children.nonEmpty

  /** True if this directive should not be repeated within a block */
  private[flexiconf] val allowOnce = flags.contains(DirectiveFlags.AllowOnce)

  def toDocumentation: String = {
    documentation ++ "\n" ++ children.map(_.toDocumentation).mkString("\n")
  }

  override def toString: String = {
    var res = name

    if (parameters.nonEmpty) {
      res ++= parameters.map(_.toString).mkString(" ", " ", "")
    }

    if (requiresBlock) {
      res ++= " {}"
    }

    res
  }
}


object DirectiveDefinition {
  /** Indicates the start of the configuration tree */
  private[flexiconf] def root(ds: Set[DirectiveDefinition] = Set.empty) = new DirectiveDefinition(name = "$root", children = ds)

  private[flexiconf] def root(ds: DirectiveDefinition*) = new DirectiveDefinition(name = "$root", children = ds.toSet)

  /** Allows inclusion of multiple, additional configuration trees */
  private[flexiconf] def include(ds: Set[DirectiveDefinition]) = new DirectiveDefinition(name = "$include", children = ds, parameters = List(Parameter("pattern")))

  /** Defines a group of directives that can be used elsewhere in the configuration tree */
  private[flexiconf] def group = new DirectiveDefinition(name = "$group", parameters = List(Parameter("name")))

  /** Includes directives from a pre-defined group in the configuration tree */
  private[flexiconf] def use(ds: Set[DirectiveDefinition]) = new DirectiveDefinition( name = "$use", children = ds, parameters = List(Parameter("pattern")))

  /** Placeholder for errors encountered when parsing a configuration tree */
  private[flexiconf] def warning = new DirectiveDefinition(name = "$warning", parameters = List(Parameter("message")))

  /** Find the first matching directive given a list of allowed directives */
  private[flexiconf] def find(maybeDirective: MaybeDirective,
                              children: Set[DirectiveDefinition]): Option[DirectiveDefinition] = {
    children.find(maybeDirective.matches)
  }

  /** Returns a directive builder for a directive with the specified name */
  def withName(name: String) = Builder(name)

  /** Returns a directive builder for a directive with the specified name */
  private[flexiconf] def withUnsafeName(name: String) = Builder(name = name, allowInternal = true)

  case class Builder private[flexiconf] (name: String,
                                      parameters: List[Parameter] = List.empty,
                                      flags: Set[DirectiveFlag] = Set.empty,
                                      documentation: String = "",
                                      children: Set[DirectiveDefinition] = Set.empty,
                                      allowInternal: Boolean = false) {

    // Name validation
    if (name == null) {
      throw new NullPointerException
    }

    if (name.isEmpty) {
      throw new IllegalArgumentException("Name cannot be empty")
    }

    if (name.startsWith("$") && !allowInternal) {
      throw new IllegalArgumentException(s"Name '$name' cannot start with '$$'")
    }

    // Public methods

    /** Adds a new string parameter */
    def withStringArg(name: String) = {
      withArgument(name, StringArgument)
    }

    /** Adds a new boolean parameter */
    def withBoolArg(name: String) = {
      withArgument(name, BoolArgument)
    }

    /** Adds a new integer parameter */
    def withIntArg(name: String) = {
      withArgument(name, IntArgument)
    }

    /** Adds a new decimal parameter */
    def withDecimalArg(name: String) = {
      withArgument(name, DecimalArgument)
    }

    /** Adds a new duration parameter */
    def withDurationArg(name: String) = {
      withArgument(name, DurationArgument)
    }

    /** Adds a new percentage parameter */
    def withPercentageArg(name: String) = {
      withArgument(name, PercentageArgument)
    }

    /** Add documentation for this directive */
    def withDocumentation(documentation: String) = {
      copy(documentation = documentation)
    }

    /** Allow a directive to be used multiple times within a scope */
    def allowOnce() = {
      copy(flags = flags + DirectiveFlags.AllowOnce)
    }

    /** Allows one or more child directives within a block supplied to this directive */
    @varargs
    def withDirectives(ds: DirectiveDefinition*) = {
      copy(children = children ++ ds)
    }

    /** Returns new Directive with the previously defined options */
    def build = DirectiveDefinition(name, parameters, flags, documentation, children)


    // Private methods

    /** Adds a new parameter with the provided name and type */
    private def withArgument(name: String, kind: ArgumentKind[_]) = {
      if (name == null) {
        throw new NullPointerException
      }

      if (name.isEmpty) {
        throw new IllegalArgumentException("Name cannot be empty")
      }

      copy(parameters = parameters :+ Parameter(name, kind))
    }

    /** Private builder features only for use with schema */
    private[flexiconf] def withParameters(params: List[Parameter]) = {
      copy(parameters = params)
    }

    /** Private builder features only for use with schema */
    private[flexiconf] def withFlags(flags: Set[DirectiveFlag]) = {
      copy(flags = flags)
    }
  }
}
