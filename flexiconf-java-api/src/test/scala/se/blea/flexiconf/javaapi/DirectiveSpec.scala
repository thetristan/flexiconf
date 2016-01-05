package se.blea.flexiconf.javaapi

import org.scalatest.{FlatSpec, Matchers}
import se.blea.flexiconf
import se.blea.flexiconf._

class DirectiveSpec extends FlatSpec with Matchers {
  val arguments = List(
    StringArg("foo"),
    IntArg("123"),
    BoolArg("off"),
    DecimalArg("10.1"),
    PercentageArg("100%"),
    DurationArg("15m")
  )

  val d1 = DefaultDefinition("foo")
  val d2 = DefaultDefinition("bar")
  val d3 = DefaultDefinition("baz", List(StringParam("arg1")))

  val root = DefaultDefinition(
    name = "directive",
    params = List(
      StringParam("string"),
      IntParam("int"),
      BoolParam("boolean"),
      DecimalParam("decimal"),
      PercentageParam("percentage"),
      DurationParam("duration")
    ),
    definitions = Set(d1, d2, d3)
  )

  val directive = new Directive(DefaultDirective(root, arguments, List(DefaultDirective(d1), DefaultDirective(d2))))

  behavior of "getName"

  it should "return name" in {
    directive.getName shouldEqual "directive"
  }


  behavior of "getArgs"

  it should "return args in order" in {
    // scalastyle:off magic.number
    directive.getArgs.size() shouldEqual 6
    directive.getArgs.get(0).getName shouldEqual "string"
    directive.getArgs.get(1).getName shouldEqual "int"
    directive.getArgs.get(2).getName shouldEqual "boolean"
    directive.getArgs.get(3).getName shouldEqual "decimal"
    directive.getArgs.get(4).getName shouldEqual "percentage"
    directive.getArgs.get(5).getName shouldEqual "duration"
    // scalastyle:off magic.number
  }

  behavior of "hasDirective"

  it should "return true for existing directives" in {
    directive.contains("foo") shouldBe true
  }

  it should "return false for directives that don't exist" in {
    directive.contains("qux") shouldBe false
  }

  behavior of "getDirectives"

  it should "return java.util.List" in {
    directive.getDirectives.isInstanceOf[java.util.List[flexiconf.javaapi.Directive]] shouldBe true
  }

  it should "return wrapped directives" in {
    directive.getDirectives.size() shouldEqual 2
    directive.getDirectives.get(0).isInstanceOf[flexiconf.javaapi.Directive] shouldBe true
  }

  it should "return filtered directives in order" in {
    directive.getDirectives("foo", "bar").size() shouldEqual 2
    directive.getDirectives("foo", "bar").get(0).getName shouldEqual "foo"
    directive.getDirectives("foo", "bar").get(1).getName shouldEqual "bar"
  }


  behavior of "getDirective"

  it should "return a wrapped directive" in {
    directive.getDirective("foo").isInstanceOf[flexiconf.javaapi.Directive] shouldBe true
  }

  it should "return a single directive" in {
    directive.getDirective("foo").getName shouldEqual "foo"
  }

  it should "return a null directive if the directive is allowed but doesn't exist" in {
    directive.getDirective("baz").getName shouldEqual "unknown"

  }

  it should "throw an exception if the directive doesn't exist" in {
    intercept[IllegalStateException] {
      directive.getDirective("qux")
    }
  }

  behavior of "hasArg"

  it should "return true for existing arguments" in {
    directive.hasArg("boolean") shouldBe true
  }

  it should "return true for arguments that exist on null directives" in {
    directive.getDirective("baz").hasArg("arg1") shouldBe true
  }

  it should "return false for arguments that don't exist" in {
    directive.hasArg("non-existent") shouldBe false
  }

  behavior of "argument getters"

  it should "return zero values for getting arguments on null directives" in {
    directive.getDirective("baz").getStringArg("arg1") shouldEqual ""
  }

  it should "throw an exception if the argument doesn't exist on null directives" in {
    intercept[IllegalStateException] {
      directive.getDirective("baz").getStringArg("non-existent")
    }
  }

  it should "return boolean argument values" in {
    directive.getBoolArg("boolean") shouldEqual false
  }

  it should "return int argument values" in {
    directive.getIntArg("int") shouldEqual 123
  }

  it should "return decimal argument values" in {
    directive.getDecimalArg("decimal") shouldEqual 10.1
  }

  it should "return string argument values" in {
    directive.getStringArg("string") shouldEqual "foo"
  }

  it should "return percentage argument values" in {
    directive.getPercentageArg("percentage") shouldEqual 1.0
  }

  it should "return duration argument values" in {
    directive.getDurationArg("duration") shouldEqual 900000
  }

  it should "throw an exception if the argument doesn't exist" in {
    intercept[IllegalStateException] {
      directive.getStringArg("non-existent")
    }
  }
}
