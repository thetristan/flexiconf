package se.blea.flexiconf.parser

import org.antlr.v4.runtime.ParserRuleContext
import se.blea.flexiconf._
import se.blea.flexiconf.parser.gen.SchemaParser._
import se.blea.flexiconf.parser.gen.SchemaParserBaseVisitor
import se.blea.flexiconf.util.{FileUtil, Stack}

import scala.collection.JavaConversions._


/** Visitor for parsing schemas */
private[flexiconf] class SchemaVisitor(options: SchemaVisitorOptions,
                                       stack: Stack[DefaultSchemaNode] = Stack.empty,
                                       visitorCtx: SchemaVisitorContext = SchemaVisitorContext())
  extends SchemaParserBaseVisitor[Option[DefaultSchemaNode]] {

  /** Entry point for a schema file */
  override def visitDocument(ctx: DocumentContext): Option[DefaultSchemaNode] = {
    val name = stack.peek.map(_.name) getOrElse "$root"
    val params = stack.peek.map(_.parameters) getOrElse List.empty
    val flags = stack.peek.map(_.flags) getOrElse Set.empty
    val document = DefaultSchemaNode(name, params, sourceFromContext(ctx), flags)

    stack.enterFrame(document) {
      Some(document.copy(children = visitDirectives(ctx.directiveList())))
    }
  }

  /** Resolves all included files and parses them, adding new directives to the configuration tree */
  override def visitInclude(ctx: IncludeContext): Option[DefaultSchemaNode] = {
    val includePattern = ctx.stringArgument.getText
    val currentPath = stack.peek.map(_.source.sourceFile).getOrElse("")
    val includePath = FileUtil.resolvePath(currentPath, includePattern).toString

    Parser.streamFromSourceFile(includePath) flatMap { inputStream =>
      val parser = Parser.antlrSchemaParserFromStream(inputStream)

      new SchemaVisitor(options.copy(sourceFile = includePath), stack).visitDocument(parser.document()) map { list =>
        DefaultSchemaNode(name = "$include",
          parameters = List.empty,
          source = sourceFromContext(ctx),
          children = list.children)
      }
    } orElse {
      val source = sourceFromContext(ctx)
      val reason = s"File '$includePath' does not exist"
      throw new IllegalStateException(s"$reason at $source")
    }
  }

  /** Saves the directives referenced within this group on the stack for lookup later */
  override def visitGroup(ctx: GroupContext): Option[DefaultSchemaNode] = {
    val name = ctx.stringArgument.getText
    val directives = ctx.directiveList

    addGroup(name, directives)

    Some(DefaultSchemaNode("$group",
      List.empty,
      sourceFromContext(ctx)))
  }

  /** Inserts the directive list specified by the given group if it exists */
  override def visitUse(ctx: UseContext): Option[DefaultSchemaNode] = {
    val name = ctx.stringArgument.getText
    val allowedDirectives = stack.peek.getOrElse(Set.empty)
    val use = DefaultSchemaNode("$use", List.empty, sourceFromContext(ctx))

    findGroup(name) map { ctx =>
      use.copy(children = visitDirectives(ctx))
    } orElse {
      val source = sourceFromContext(ctx)
      val reason = s"Unknown group: $name"

      if (options.allowMissingGroups) {
        Some(use.copy(children = List(DefaultSchemaNode("$warning", List(Parameter(reason)), source))))
      } else {
        throw new IllegalStateException(s"$reason at $source")
      }
    }
  }


  /** Searches and returns a config node if a matching directive can be found */
  override def visitUserDirective(ctx: UserDirectiveContext): Option[DefaultSchemaNode] = {
    val name = ctx.directiveName.getText
    val source = sourceFromContext(ctx)
    val parameters = ParameterVisitor(ctx.parameterList())
    val flags = DirectiveFlagListVisitor(ctx.flagList())
    val docs = DocVisitor(ctx.documentationBlock()).mkString("\n")
    val node = DefaultSchemaNode(name, parameters, sourceFromContext(ctx), flags, docs)

    stack.enterFrame(node) {
      Some(node.copy(children = visitDirectives(ctx.directiveList())))
    }
  }


  /** * Returns list of nodes from a context containing possible directives */
  def visitDirectives(ctx: ParserRuleContext): List[DefaultSchemaNode] = ctx match {
    case ctx: DirectiveListContext => (ctx.directive flatMap visitDirective).toList
    case _ => List.empty
  }

  /** Records a group of directives with the given name in the current frame */
  def addGroup(name: String, directives: DirectiveListContext): Unit = {
    visitorCtx.addGroup(stack.peek.get, name, directives)
  }

  /** Finds a group of directives in the current stack */
  def findGroup(name: String): Option[DirectiveListContext] = {
    stack.find { f =>
      visitorCtx.groupsByNode.get(f).exists(_.contains(name))
    } flatMap { f =>
      visitorCtx.groupsByNode(f).get(name)
    }
  }

  /** Returns a new Source object based on the provided context */
  def sourceFromContext(ctx: ParserRuleContext): Source = {
    Source(options.sourceFile, ctx.getStart.getLine, ctx.getStart.getCharPositionInLine)
  }
}
