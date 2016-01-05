package se.blea.flexiconf.parser

import org.antlr.v4.runtime.ParserRuleContext
import se.blea.flexiconf._
import se.blea.flexiconf.parser.gen.SchemaParser._
import se.blea.flexiconf.parser.gen.SchemaParserBaseVisitor
import se.blea.flexiconf.util._

import scala.collection.JavaConversions._
import scala.util.{Failure, Success}


/** Visitor for parsing schemas */
private[flexiconf] case class SchemaVisitor(
  sourceFile: String,
  stack: Stack[Node] = Stack.empty
) extends SchemaParserBaseVisitor[Node] {

  /** Entry point for a schema file */
  override def visitDocument(ctx: DocumentContext): Node = {
    var root = RootNode(sourceFromContext(ctx))

    stack.push(root)

    Option(ctx.directiveList()) foreach { directiveListCtx =>
      directiveListCtx.directive() foreach { d =>
        val next = visitDirective(d)
        val nodes = root.nodes.getOrElse(Nil) :+ next
        root = root.copy(nodes = Some(nodes))
        stack.swap(root)
      }
    }

    stack.pop()
  }

  /** Resolves all included files and parses them, adding new directives to the configuration tree */
  override def visitInclude(ctx: IncludeContext): Node = {
    val source = sourceFromContext(ctx)
    val includePattern = ctx.stringArgument.getText
    val currentPath = stack.peek.map(_.source.sourceFile).getOrElse(throw new IllegalStateException("Missing source"))
    val includePath = FileUtil.resolvePath(currentPath, includePattern).toString

    Parser.streamFromSourceFile(includePath) match {
      case Success(inputStream) =>
        val parser = Parser.antlrSchemaParserFromStream(inputStream)
        val include = IncludeNode(source, includePath)
        val visitor = SchemaVisitor(includePath, stack)

        try {
          stack.push(include)
          include.copy(nodes = Some(List(visitor.visitDocument(parser.document()))))
        } finally {
          stack.pop()
        }

      case Failure(ex) =>
        val reason = s"Couldn't open $includePath: ${ex.getMessage}"
        throw new IllegalStateException(s"$reason at $source")
    }
  }

  /** Saves the directives referenced within this group on the stack for lookup later */
  override def visitGroup(ctx: GroupContext): Node = {
    var group = GroupNode(sourceFromContext(ctx), ctx.stringArgument.getText)

    stack.push(group)

    Option(ctx.directiveList()) foreach { directiveListCtx =>
      directiveListCtx.directive() foreach { d =>
        val next = visitDirective(d)
        val nodes = group.nodes.getOrElse(Nil) :+ next
        group = group.copy(nodes = Some(nodes))
        stack.swap(group)
      }
    }

    stack.pop()
  }

  /** Inserts the directive list specified by the given group if it exists */
  override def visitUse(ctx: UseContext): Node = {
    val source = sourceFromContext(ctx)
    val name = ctx.stringArgument.getText
    val use = UseNode(source, name)
    val treeNodes = stack.filter(_.isInstanceOf[TreeNode])
    val children = treeNodes.asInstanceOf[List[TreeNode]].flatMap(_.nodes.getOrElse(Nil))
    val groupNodes = children.collectFirst {
      case g: GroupNode if g.name == name => g.nodes
    }

    groupNodes match {
      case Some(nodes) =>
        use.copy(nodes = nodes)

      case None =>
        val reason = s"Undefined group $name"
        throw new IllegalStateException(s"$reason at $source")
    }
  }


  /** Searches and returns a config node if a matching directive can be found */
  override def visitDefinition(ctx: DefinitionContext): Node = {
    var definition = DefinitionNode(
      name = ctx.definitionName.getText,
      parameters = ParameterVisitor(ctx.parameterList()),
      flags = FlagVisitor(ctx.flagList()),
      documentation = DocVisitor(ctx.documentationBlock()).mkString("\n"),
      source = sourceFromContext(ctx)
    )

    stack.push(definition)

    Option(ctx.directiveList()) foreach { directiveListCtx =>
      directiveListCtx.directive() foreach { d =>
        val next = visitDirective(d)
        val nodes = definition.nodes.getOrElse(Nil) :+ next
        definition = definition.copy(nodes = Some(nodes))
        stack.swap(definition)
      }
    }

    stack.pop()
  }

  object FlagVisitor extends SchemaParserBaseVisitor[FlagNode] {
    def apply(ctx: ParserRuleContext): Set[FlagNode] = ctx match {
      case ctx: FlagListContext => (ctx.flag map visitFlag).toSet
      case _ => Set.empty
    }

    override def visitFlag(ctx: FlagContext): FlagNode = {
      FlagNode(sourceFromContext(ctx), ctx.flagName.getText, Option(ctx.flagValue()).map(_.getText))
    }
  }

  object ParameterVisitor extends SchemaParserBaseVisitor[ParameterNode] {
    def apply(ctx: ParserRuleContext): List[ParameterNode] = ctx match {
      case ctx: ParameterListContext => (ctx.parameter map visitParameter).toList
      case _ => Nil
    }

    override def visitParameter(ctx: ParameterContext): ParameterNode = {
      ParameterNode(sourceFromContext(ctx), ctx.parameterName.getText, Type.fromString(ctx.parameterType.getText))
    }
  }

  /** Returns a new Source object based on the provided context */
  def sourceFromContext(ctx: ParserRuleContext): Source = {
    Source(sourceFile, ctx.getStart.getLine, ctx.getStart.getCharPositionInLine)
  }
}
