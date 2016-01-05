package se.blea.flexiconf.parser

import java.io.FileNotFoundException

import org.antlr.v4.runtime.ParserRuleContext
import se.blea.flexiconf._
import se.blea.flexiconf.parser.gen.ConfigBaseVisitor
import se.blea.flexiconf.parser.gen.ConfigParser._
import se.blea.flexiconf.util.{FileUtil, Stack}

import scala.collection.JavaConversions._
import scala.util.{Failure, Success}


/** Converts ASTs from ANTLR into usable configuration trees */
private[flexiconf] case class ConfigVisitor(
  sourceFile: String,
  options: ConfigOptions = ConfigOptions(),
  stack: Stack[Node] = Stack.empty
) extends ConfigBaseVisitor[Node] {

  /** Entry point for a configuration file */
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

  def visitDocument(root: RootNode, nodes: List[DirectiveContext]): RootNode = {
    nodes match {
      case n :: ns =>
        val nodes = root.nodes.map(_ :+ visitDirective(n))
        visitDocument(root.copy(nodes = nodes), ns)
      case Nil => root
    }
  }

  /** Resolves all included files and parses them, adding new directives to the configuration tree */
  override def visitInclude(ctx: IncludeContext): Node = {
    val source = sourceFromContext(ctx)
    val includePattern = ctx.stringArgument.getText
    val currentPath = stack.peek.map(_.source.sourceFile).getOrElse(throw new IllegalStateException("Empty stack when determining current path"))
    val includePath = FileUtil.resolvePath(currentPath, includePattern).toString

    Parser.streamFromSourceFile(includePath) match {
      case Success(inputStream) =>
        val parser = Parser.antlrConfigParserFromStream(inputStream)
        val include = IncludeNode(source, includePath)
        val visitor = ConfigVisitor(includePath, options, stack)

        stack.push(include)
        include.copy(nodes = Some(List(visitor.visitDocument(parser.document()))))
        stack.pop()

      case Failure(ex) =>
        val reason = ex.getMessage
        ex match {
          case _: FileNotFoundException =>
            if (options.allowMissingIncludes) {
              WarningNode(source, reason)
            } else {
              throw new IllegalStateException(s"$reason at $source")
            }
          case _ =>
            throw new IllegalStateException(s"$reason at $source")
        }
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
    val treeNodes: List[TreeNode] = stack.filter(_.isInstanceOf[TreeNode]).asInstanceOf[List[TreeNode]]
    val groupNodes = treeNodes.flatMap(_.nodes.getOrElse(Nil)) collectFirst {
      case g: GroupNode if g.name == name => g.nodes
    }

    groupNodes match {
      case Some(nodes) =>
        use.copy(nodes = nodes)

      case None =>
        val reason = s"Undefined group $name"

        if (options.allowMissingGroups) {
          WarningNode(source, reason)
        } else {
          throw new IllegalStateException(s"$reason at $source")
        }
    }
  }

  /** Searches and returns a config node if a matching directive can be found */
  override def visitUserDirective(ctx: UserDirectiveContext): Node = {
    val childDirectives = Option(ctx.directiveList())
    var directive = DirectiveNode(
      name = ctx.directiveName.getText,
      arguments = ArgVisitor(ctx.argumentList),
      source = sourceFromContext(ctx),
      nodes = childDirectives.map(_ => Nil)
    )

    stack.push(directive)

    childDirectives foreach { directiveListCtx =>
      directiveListCtx.directive() foreach { d =>
        val next = visitDirective(d)
        val nodes = directive.nodes.getOrElse(Nil) :+ next
        directive = directive.copy(nodes = Some(nodes))
        stack.swap(directive)
      }
    }

    stack.pop()
  }

  object ArgVisitor extends ConfigBaseVisitor[ArgNode] {
    def apply(ctx: ParserRuleContext): List[ArgNode] = ctx match {
      case ctx: ArgumentListContext => (ctx.argument map visitArgument).toList
      case _ => Nil
    }

    // scalastyle:off
    override def visitUnquotedStringValue(ctx: UnquotedStringValueContext): ArgNode = ArgNode(sourceFromContext(ctx), ctx.getText, StringType)
    override def visitQuotedStringValue(ctx: QuotedStringValueContext): ArgNode = ArgNode(sourceFromContext(ctx), ctx.getText.substring(1, ctx.getText.size - 1), StringType)
    override def visitIntegerValue(ctx: IntegerValueContext): ArgNode = ArgNode(sourceFromContext(ctx), ctx.getText, IntType)
    override def visitBooleanValue(ctx: BooleanValueContext): ArgNode = ArgNode(sourceFromContext(ctx), ctx.getText, BoolType)
    override def visitDecimalValue(ctx: DecimalValueContext): ArgNode = ArgNode(sourceFromContext(ctx), ctx.getText, DecimalType)
    override def visitDurationValue(ctx: DurationValueContext): ArgNode = ArgNode(sourceFromContext(ctx), ctx.getText, DurationType)
    override def visitPercentageValue(ctx: PercentageValueContext): ArgNode = ArgNode(sourceFromContext(ctx), ctx.getText, PercentageType)
    // scalastyle:on
  }

  /** Returns a new Source object based on the provided context */
  def sourceFromContext(ctx: ParserRuleContext): Source = {
    Source(sourceFile, ctx.getStart.getLine, ctx.getStart.getCharPositionInLine)
  }
}
