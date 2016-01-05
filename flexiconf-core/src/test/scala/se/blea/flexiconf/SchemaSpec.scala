package se.blea.flexiconf

import java.io.ByteArrayInputStream

import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream}
import org.apache.commons.io.input.NullInputStream
import org.scalatest.{FlatSpec, Matchers}
import se.blea.flexiconf.parser.gen.{SchemaLexer, SchemaParser}
import se.blea.flexiconf.parser.{RootNode, SchemaVisitor}

/** Test cases for Schema */
class SchemaSpec extends FlatSpec with Matchers {
  private def schema(inputString: String) = {
    val bytes = inputString.getBytes
    val input = new ANTLRInputStream(new ByteArrayInputStream(bytes))
    val lexer = new SchemaLexer(input)
    val tokens = new CommonTokenStream(lexer)
    val parser = new SchemaParser(tokens)
    val document = parser.document()
    val visitor = new SchemaVisitor("test")

    visitor.visitDocument(document)
  }

  behavior of "#visitDocument"

  it should "return a tree that includes a root node" in {
    val result = schema(
      """
        |foo val:Int;
        |bar val:String;
        |baz val:Decimal [once];
        |qux val:Bool;
      """.stripMargin)

    assert(result.isInstanceOf[RootNode])

    // scalastyle:off magic.number
//    result.get.definitions shouldEqual Set(
//      DefaultDefinition(name = "foo", source = Some(Source("test", 2, 0)), parameters = List(Parameter("val", IntArgument))),
//      DefaultDefinition(name = "bar", source = Some(Source("test", 3, 0)), parameters = List(Parameter("val", StringArgument))),
//      DefaultDefinition(name = "baz", source = Some(Source("test", 4, 0)), flags = Set(AllowOnce), parameters = List(Parameter("val", DecimalArgument))),
//      DefaultDefinition(name = "qux", source = Some(Source("test", 5, 0)), parameters = List(Parameter("val", BoolArgument))))
    // scalastyle:on magic.number
  }
}
