package se.blea.flexiconf

import scala.annotation.varargs

/**
 * Defines the name, parameters, and allowed child directives for a configuration directive
 *
 * User-defined directive names may consist of any non-whitespace characters as long as they
 * do not start with '$' so that they can be identified separately from built-in directives that
 * start with '$' (e.g. \$root, \$use, \$group, \$include).
 */
private[flexiconf] case class DirectiveDefinition private[flexiconf](name: String,
                                                                     parameters: List[Parameter] = List.empty,
                                                                     flags: Set[DirectiveFlag] = Set.empty,
                                                                     documentation: String = "",
                                                                     children: Set[DirectiveDefinition] = Set.empty) {

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

  /** Find the first matching directive given a list of allowed directives */
  private[flexiconf] def find(maybeDirective: MaybeDirective,
                              children: Set[DirectiveDefinition]): Option[DirectiveDefinition] = {
    children.find(maybeDirective.matches)
  }

  /** Returns a directive builder for a directive with the specified name */
  def withName(name: String): Builder = Builder(name)

  /** Returns a directive builder for a directive with the specified name */
  private[flexiconf] def withUnsafeName(name: String) = Builder(name = name, allowInternal = true)

  case class Builder private[flexiconf] (name: String,
                                         parameters: List[Parameter] = List.empty,
                                         flags: Set[DirectiveFlag] = Set.empty,
                                         documentation: String = "",
                                         children: Set[DirectiveDefinition] = Set.empty,
                                         allowInternal: Boolean = false) {
    if (name.isEmpty) {
      throw new IllegalArgumentException("Name cannot be empty")
    }

    if (name.startsWith("$") && !allowInternal) {
      throw new IllegalArgumentException(s"Name '$name' cannot start with '$$'")
    }

    // Public methods

    /** Adds a new string parameter */
    def withStringArg(name: String): Builder = {
      withArgument(name, StringArgument)
    }

    /** Adds a new boolean parameter */
    def withBoolArg(name: String): Builder = {
      withArgument(name, BoolArgument)
    }

    /** Adds a new integer parameter */
    def withIntArg(name: String): Builder = {
      withArgument(name, IntArgument)
    }

    /** Adds a new decimal parameter */
    def withDecimalArg(name: String): Builder = {
      withArgument(name, DecimalArgument)
    }

    /** Adds a new duration parameter */
    def withDurationArg(name: String): Builder = {
      withArgument(name, DurationArgument)
    }

    /** Adds a new percentage parameter */
    def withPercentageArg(name: String): Builder = {
      withArgument(name, PercentageArgument)
    }

    /** Add documentation for this directive */
    def withDocumentation(documentation: String): Builder = {
      copy(documentation = documentation)
    }

    /** Allow a directive to be used multiple times within a scope */
    def allowOnce(): Builder = {
      copy(flags = flags + DirectiveFlags.AllowOnce)
    }

    /** Allows one or more child directives within a block supplied to this directive */
    @varargs
    def withDirectives(ds: DirectiveDefinition*): Builder = {
      copy(children = children ++ ds)
    }

    /** Returns new Directive with the previously defined options */
    def build: DirectiveDefinition = DirectiveDefinition(name, parameters, flags, documentation, children)


    // Private methods

    /** Adds a new parameter with the provided name and type */
    private def withArgument(name: String, kind: ArgumentKind[_]) = {
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
