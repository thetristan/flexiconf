package se.blea.flexiconf

import java.io.ByteArrayInputStream

import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ANTLRInputStream
import org.apache.commons.io.input.NullInputStream
import se.blea.flexiconf.parser._
import se.blea.flexiconf.parser.gen.{ConfigParser, ConfigLexer, SchemaParser, SchemaLexer}
import se.blea.flexiconf.util.Stack

/** Helper methods for working with config parsers */
trait ConfigHelpers {
//  def makeStack(node: Node): Stack[NodeWithContext] = Stack(List(NodeWithContext(node)))
//  def visitor(opts: ConfigOptions = defaultOptions, stack: Stack[NodeWithContext] = Stack.empty): ConfigVisitor = ConfigVisitor(opts.visitorOpts, stack)
//  def defaultOptions : ConfigOptions = ConfigOptions.withSourceFile(Configs.BASIC_TREE)

//  private[flexiconf] def node(d: Definition): ConfigNode = ConfigNode(d, List.empty, Some(Source("", 0, 0)))
//  private[flexiconf] def node(n: String): ConfigNode = node(DefaultDefinition.withName(n).build)
//  private[flexiconf] def rootNode(ds: Definition*): ConfigNode = node(BuiltInDirectives.root(ds:_*))
//  private[flexiconf] def nodeWithSchema(inputString: String) = node(schema(inputString))
//  private[flexiconf] def emptyStackWithSchema(inputString: String) = makeStack(nodeWithSchema(inputString))

  private[flexiconf] def schema(inputString: String) = {
    val bytes = inputString.getBytes
    val input = new ANTLRInputStream(new ByteArrayInputStream(bytes))
    val lexer = new SchemaLexer(input)
    val tokens = new CommonTokenStream(lexer)
    val parser = new SchemaParser(tokens)
    val document = parser.document()
    val visitor = new SchemaVisitor("test")

    visitor.visitDocument(document)
  }

  private[flexiconf] def parse(inputString: String) = {
    val bytes = inputString.getBytes
    val input = new ANTLRInputStream(new ByteArrayInputStream(bytes))
    val lexer = new ConfigLexer(input)
    val tokens = new CommonTokenStream(lexer)

    new ConfigParser(tokens)
  }
}
