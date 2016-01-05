package se.blea.flexiconf.parser

import java.io.File

import org.apache.commons.io.input.NullInputStream
import org.scalatest.{FlatSpec, Matchers}
import se.blea.flexiconf.util.Stack
import se.blea.flexiconf._

/** Test cases for config parsing */
class ConfigVisitorSpec extends FlatSpec with Matchers with ConfigHelpers {

  behavior of "#sourceFromContext"

  it should "return a Source object based on the provided context" in {
    val ctx = parse("\n\nfoo bar baz")
    val visitor = ConfigVisitor("test.conf")

    val Source(file, line, char) = visitor.sourceFromContext(ctx.argumentList().argument(2))
    file shouldEqual "test.conf"
    line shouldEqual 3  // scalastyle:ignore magic.number
    char shouldEqual 8  // scalastyle:ignore magic.number
  }


  behavior of "#visitInclude"

  it should "return an include directive node for includes" in {
    val ctx = parse(s"include includes/include_a.conf;")
    val visitor = ConfigVisitor(Configs.BASIC_TREE, stack = Stack(RootNode(Source(Configs.BASIC_TREE, 0, 0))))

    val IncludeNode(source, path, _) = visitor.visitInclude(ctx.include()).asInstanceOf[IncludeNode]
    source shouldEqual Source(Configs.BASIC_TREE, 1, 0)
    path shouldEqual "flexiconf-core/src/test/resources/parser/includes/include_a.conf"
  }

  it should "resolve absolute file include paths" in {
    val absPath = new File(".").toPath.toAbsolutePath.normalize.toString + "/" + Configs.BASIC_TREE
    val ctx = parse(s"include $absPath;")
    val visitor = ConfigVisitor(Configs.BASIC_TREE, stack = Stack(RootNode(Source(Configs.BASIC_TREE, 0, 0))))

    val IncludeNode(source, path, _) = visitor.visitInclude(ctx.include()).asInstanceOf[IncludeNode]
    source shouldEqual Source(Configs.BASIC_TREE, 1, 0)
    path shouldEqual absPath
  }

  it should "throw an exception when an included file can't be found" in {
    intercept[IllegalStateException] {
      val ctx = parse("include foo/bar/baz_*.conf;")
      val visitor = ConfigVisitor(Configs.BASIC_TREE, stack = Stack(RootNode(Source(Configs.BASIC_TREE, 0, 0))))

      visitor.visitInclude(ctx.include())
    }
  }

  it should "return a warning node when an included file can't be found and warnings are enabled" in {
    val ctx = parse("include foo/bar/baz_*.conf;")
    val visitor = ConfigVisitor(Configs.BASIC_TREE,
      ConfigOptions(allowMissingIncludes = true),
      Stack(RootNode(Source(Configs.BASIC_TREE, 0, 0))))

    val WarningNode(source, warning) = visitor.visitInclude(ctx.include()).asInstanceOf[WarningNode]
    source shouldEqual Source(Configs.BASIC_TREE, 1, 0)
    warning shouldEqual "File 'flexiconf-core/src/test/resources/parser/foo/bar/baz_*.conf' does not exist"
  }


  behavior of "#visitGroup"

  it should "return a group directive node for groups" in {
    val ctx = parse("group my_group { foo 123; }")
    val visitor = ConfigVisitor("test.conf")

    val GroupNode(source, name, Some(nodes)) = visitor.visitGroup(ctx.group()).asInstanceOf[GroupNode]
    source shouldEqual Source("test.conf", 1, 0)
    name shouldEqual "my_group"
    nodes.length shouldEqual 1
  }


  behavior of "#visitUse"

  it should "return a use directive node for uses" in {
      val ctx = parse("group my_group { foo 123; } use my_group;")
      val visitor = ConfigVisitor(Configs.BASIC_TREE)

      val RootNode(_, Some(List(_, UseNode(source, name, _)))) = visitor.visitDocument(ctx.document())
      source shouldEqual Source(Configs.BASIC_TREE, 1, 28)
      name shouldEqual "my_group"
  }

  it should "return a warning node when an unknown group is encountered and missing groups are ignored" in {
    val ctx = parse("use my_group;")
    val visitor = ConfigVisitor("test.conf", ConfigOptions(allowMissingGroups = true), Stack(RootNode(Source(Configs.BASIC_TREE, 0, 0))))

    val WarningNode(source, warning) = visitor.visitDirectiveList(ctx.directiveList()).asInstanceOf[WarningNode]
    source shouldEqual Source("test.conf", 1, 0)
    warning shouldEqual "Undefined group my_group"
  }

  it should "throw an exception when no groups are defined and missing groups are not ignored" in {
    intercept[IllegalStateException] {
      val ctx = parse("use my_group;")
      val visitor = ConfigVisitor(Configs.BASIC_TREE, stack = Stack(RootNode(Source(Configs.BASIC_TREE, 0, 0))))

      visitor.visitDirective(ctx.directive())
    }
  }


  behavior of "#visitUserDirective"

  it should "return a user directive node for non-built-in directives" in {
    val ctx = parse("foo 123;")
    val visitor = ConfigVisitor("test.conf")
    val DirectiveNode(source, name, args, _) = visitor.visitDirective(ctx.directive())

    source shouldEqual Source("test.conf", 1, 0)
    name shouldEqual "foo"
    args shouldEqual List(ArgNode(Source("test.conf", 1, 4), "123", IntType))
  }
}
