package se.blea.flexiconf.parser

import org.scalatest.{FlatSpec, Matchers}
import se.blea.flexiconf._

/** Test cases for argument parsing */
class ArgumentVisitorSpec extends FlatSpec with Matchers with ConfigHelpers {

  private def argumentVisitor = ConfigVisitor("test").ArgVisitor

  behavior of "#apply"

  it should "return an empty list of arguments when visiting an empty argument lists" in {
    val result = argumentVisitor(null) // scalastyle:ignore
    assert(result.size == 0)
  }

  it should "return list of arguments when visiting argument lists" in {
    // scalastyle:off magic.number
    val ctx = parse( """1 off "true" 500.2 foo bar 100ms 25% -600""")
    val result = argumentVisitor(ctx.argumentList)

    assert(result.size == 9)
    assert(result(0).kind == IntType)
    assert(result(1).kind == BoolType)
    assert(result(2).kind == StringType)
    assert(result(3).kind == DecimalType)
    assert(result(4).kind == StringType)
    assert(result(5).kind == StringType)
    assert(result(6).kind == DurationType)
    assert(result(7).kind == PercentageType)
    assert(result(8).kind == IntType)
    // scalastyle:on magic.number
  }

  it should "return string arguments for quoted string arguments" in {
    val ctx = parse(""" "foo" """).argumentList()
    val result = argumentVisitor(ctx)
    assert(result(0).kind == StringType)
    assert(result(0).value == "foo")
  }

  it should "return string arguments for double quoted string arguments with escaped quotes in them" in {
    val ctx = parse("\"foo bar\\t \\r \\n \\\\ \\\"\"").argumentList()
    val result = argumentVisitor(ctx)
    assert(result(0).kind == StringType)
    assert(result(0).value == "foo bar\\t \\r \\n \\\\ \\\"")
  }

  it should "return string arguments for single quoted string arguments with escaped quotes in them" in {
    val ctx = parse("'foo bar\\t \\r \\n \\\\ \\''").argumentList()
    val result = argumentVisitor(ctx)
    assert(result(0).kind == StringType)
    assert(result(0).value == "foo bar\\t \\r \\n \\\\ \\'")
  }

  it should "return string arguments for quoted string arguments with escaped slashes in them" in {
    val ctx = parse("'foo bar\\t \\r \\n \\\\ \\'\\\\'").argumentList()
    val result = argumentVisitor(ctx)
    assert(result(0).kind == StringType)
    assert(result(0).value == "foo bar\\t \\r \\n \\\\ \\'\\\\")
  }

  it should "return string arguments for unquoted string arguments" in {
    val ctx = parse("foo").argumentList()
    val result = argumentVisitor(ctx)(0)
    (result.kind, result.value) shouldEqual (StringType, "foo")
  }

  it should "visit quoted booleans and return string arguments" in {
    val result = argumentVisitor(parse("\"off\"").argumentList)(0)
    (result.kind, result.value) shouldEqual (StringType, "off")
  }

  it should "return boolean arguments for boolean values" in {
    val result = argumentVisitor(parse("on").argumentList)(0)
    (result.kind, result.value) shouldEqual (BoolType, "on")
  }

  // scalastyle:off magic.number
  it should "return integer arguments for integer values" in {
    val result = argumentVisitor(parse("10001").argumentList)(0)
    (result.kind, result.value) shouldEqual (IntType, "10001")
  }

  it should "return integer arguments for negative integer values" in {
    val result = argumentVisitor(parse("-10001").argumentList)(0)
    (result.kind, result.value) shouldEqual (IntType, "-10001")
  }

  it should "return decimal arguments for decimal values" in {
    val result = argumentVisitor(parse("0.300").argumentList)(0)
    (result.kind, result.value) shouldEqual (DecimalType, "0.300")
  }

  it should "return decimal arguments for negative decimal values" in {
    val result = argumentVisitor(parse("-0.300").argumentList)(0)
    (result.kind, result.value) shouldEqual (DecimalType, "-0.300")
  }

  it should "return duration arguments for duration values" in {
    val result = argumentVisitor(parse("10s").argumentList)(0)
    (result.kind, result.value) shouldEqual (DurationType, "10s")
  }

  it should "return duration arguments for negative duration values" in {
    val result = argumentVisitor(parse("-10s").argumentList)(0)
    (result.kind, result.value) shouldEqual (DurationType, "-10s")
  }

  it should "return percentage arguments for percentage values" in {
    val result = argumentVisitor(parse("10%").argumentList)(0)
    (result.kind, result.value) shouldEqual (PercentageType, "10%")
  }

  it should "return percentage arguments for negative percentage values" in {
    val result = argumentVisitor(parse("-10%").argumentList)(0)
    (result.kind, result.value) shouldEqual (PercentageType, "-10%")
  }
  // scalastyle:on magic.number
}
