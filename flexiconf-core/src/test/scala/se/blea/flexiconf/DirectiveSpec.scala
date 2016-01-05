package se.blea.flexiconf

import org.scalatest.{FlatSpec, Matchers}

/** Test cases for Directive */
class DirectiveSpec extends FlatSpec with Matchers {
//
//  behavior of "MaybeDirective"
//
//  it should "match a node by name" in {
//    val d = DefaultDefinition(name = "test_node")
//
//    assert(MaybeDirective("test_node") matches d)
//    assert(MaybeDirective("node_test") doesNotMatch d)
//  }
//
//  it should "match a node by name and argument type" in {
//    val d = DefaultDefinition(name = "test_node",
//                              params = List(
//                                StringParam("val1"),
//                                DecimalParam("val2")))
//
//    val args = Seq(
//      StringArg("any"),
//      DecimalArg("0.0001")
//    )
//
//    val badArgs1 = Seq(
//      StringArg("any"),
//      StringArg("0.0001")
//    )
//
//    val badArgs2 = Seq(
//      StringArg("any"),
//      DecimalArg("0.0001"),
//      BoolArg("false")
//    )
//
//    val badArgs3 = Seq(
//      StringArg("any")
//    )
//
//    assert(MaybeDirective("test_node", args) matches d)
//    assert(MaybeDirective("test_node", badArgs1) doesNotMatch d)
//    assert(MaybeDirective("test_node", badArgs2) doesNotMatch d)
//    assert(MaybeDirective("test_node", badArgs3) doesNotMatch d)
//    assert(MaybeDirective("test_node") doesNotMatch d)
//  }
//
//  it should "match a node by name, args, and block allowance" in {
//    val d1 = DefaultDefinition(
//      name = "without_block",
//      params = List(IntParam("val1")))
//
//    val d2 = DefaultDefinition(
//      name = "with_block",
//      params = List(BoolParam("val1")),
//      definitions = Set(d1))
//
//    val args1 = Seq(IntArg("123"))
//    val args2 = Seq(BoolArg("true"))
//
//    assert(MaybeDirective("without_block", args1, hasBlock = false) matches d1)
//    assert(MaybeDirective("with_block", args2, hasBlock = true) matches d2)
//    assert(MaybeDirective("without_block", args1, hasBlock = true) doesNotMatch d1)
//    assert(MaybeDirective("with_block", args2, hasBlock = false) doesNotMatch d2)
//  }
}



