package se.blea.flexiconf

import java.io.ByteArrayInputStream

import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ANTLRInputStream
import se.blea.flexiconf.parser._
import se.blea.flexiconf.parser.gen.{ConfigParser, ConfigLexer, SchemaParser, SchemaLexer}
import se.blea.flexiconf.util.Stack

/** Helper methods for working with config parsers */
trait ConfigHelpers {
  private[flexiconf] def defaultOptions = ConfigOptions.withSourceFile("test")
  private[flexiconf] def node(d: DefaultDefinition): ConfigNode = ConfigNode(d, List.empty, Source("", 0, 0))
  private[flexiconf] def node(n: String): ConfigNode = node(DefaultDefinition.withName(n).build)
  private[flexiconf] def rootNode(ds: DefaultDefinition*): ConfigNode = node(BuiltInDirectives.root(ds:_*))
  private[flexiconf] def makeStack(node: ConfigNode) = Stack(List(new ConfigVisitorContext(node)))
  private[flexiconf] def visitor(opts: ConfigOptions): ConfigVisitor = new ConfigVisitor(opts.visitorOpts)
  private[flexiconf] def visitor(opts: ConfigOptions, stack: Stack[ConfigVisitorContext]): ConfigVisitor = new ConfigVisitor(opts.visitorOpts, stack)
  private[flexiconf] def nodeWithSchema(inputString: String) = node(schema(inputString))
  private[flexiconf] def emptyStackWithSchema(inputString: String) = makeStack(nodeWithSchema(inputString))

  private[flexiconf] def schema(inputString: String) = {
    val bytes = inputString.getBytes
    val input = new ANTLRInputStream(new ByteArrayInputStream(bytes))
    val lexer = new SchemaLexer(input)
    val tokens = new CommonTokenStream(lexer)
    val parser = new SchemaParser(tokens)
    val document = parser.document()

    val opts = SchemaVisitorOptions("test")
    val visitor = new SchemaVisitor(opts)

    visitor.visitDocument(document).get.toDirective
  }

  private[flexiconf] def parse(inputString: String) = {
    val bytes = inputString.getBytes
    val input = new ANTLRInputStream(new ByteArrayInputStream(bytes))
    val lexer = new ConfigLexer(input)
    val tokens = new CommonTokenStream(lexer)

    new ConfigParser(tokens)
  }
}
