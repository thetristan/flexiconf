package se.blea.flexiconf.javaapi

import org.scalatest.{Matchers, FlatSpec}
import se.blea.flexiconf
import se.blea.flexiconf._


class ArgumentSpec extends FlatSpec with Matchers {
  behavior of "getName"

  it should "return the name for an argument" in {
    val arg = new Argument("foo", StringArg("bar"))
    arg.getName shouldEqual "foo"
  }

  behavior of "getKind"

  it should "return the kind of argument as an Enum" in {
    val stringArg = new Argument("foo", StringArg("bar"))
    val intArg = new Argument("foo", IntArg("123"))
    val boolArg = new Argument("foo", BoolArg("off"))
    val decimalArg = new Argument("foo", DecimalArg("10.1"))
    val durationArg = new Argument("foo", DurationArg("15m"))
    val percentageArg = new Argument("foo", PercentageArg("100%"))

    stringArg.getKind shouldEqual ArgumentKind.String
    intArg.getKind shouldEqual ArgumentKind.Int
    boolArg.getKind shouldEqual ArgumentKind.Bool
    decimalArg.getKind shouldEqual ArgumentKind.Decimal
    durationArg.getKind shouldEqual ArgumentKind.Duration
    percentageArg.getKind shouldEqual ArgumentKind.Percentage
  }
}
