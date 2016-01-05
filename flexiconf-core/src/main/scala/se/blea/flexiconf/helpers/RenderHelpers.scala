package se.blea.flexiconf.helpers

import se.blea.flexiconf._
import se.blea.flexiconf.parser._

/**
 * Created by tblease on 8/14/15.
 */
object RenderHelpers {
  private def renderList(list: List[Renderable]): String = {
   list.headOption.map(_ => " " + list.map(_.render).mkString(" ")).getOrElse("")
  }

  private def renderSource(source: Option[Source]): String = {
   source.map(s => s" ($s)").getOrElse(" (unknown)")
  }

  implicit class RenderableTreeNode[T <: TreeNode](val n: T) extends AnyVal {
    def render: String = n.asInstanceOf[Node].render
    def renderTree(depth: Int = 0): String = n.asInstanceOf[Node].renderTree(depth)
  }

  implicit class RenderableNode[T <: Node](val n: T) extends AnyVal {
    def render: String = n match {
      case RootNode(source, _) => s"$$root ($source)"
      case IncludeNode(source, path, _) => s"$$include $path ($source)"
      case GroupNode(source, name, _) => s"$$group $name ($source)"
      case UseNode(source, name, _) => s"$$use $name ($source)"
      case DirectiveNode(source, name, args, _) => s"$$directive $name ${args.map(_.render).mkString("", " ", " ")}($source)"
      case DefinitionNode(source, name, params, _, _, _) => s"$$definition $name ${params.map(_.render).mkString("", " ", " ")}($source)"
      case ArgNode(_, value, kind) => s"$value:$kind"
      case ParameterNode(_, name, kind) => s"$name:$kind"
    }

    def renderTree(depth: Int = 0): String = {
      val renderedNode = (" " * depth) + s"> ${n.render}\n"

      val renderedChildren = n match {
        case TreeNode(t) => t.nodes.map(_.map(tn => tn.renderTree(depth + 1)).mkString).getOrElse("")
        case _ => ""
      }

      renderedNode + renderedChildren
    }
  }

  implicit class RenderableParameter(val p: Param) extends AnyVal {
   def render: String = s"${p.name}:${p.kind}"
  }

  implicit class RenderableArgument(val a: Arg) extends AnyVal {
   def render: String = a.value
  }

  implicit class RenderableSchemaTree(val s: Schema) extends AnyVal {
    def render: String = "$root"
    def renderTree(depth: Int = 0): String = {
      val renderedNode = (" " * depth) + s"> ${s.render}\n"
      val renderedChildren = s.definitions.map(d => d.renderTree(depth + 1)).mkString

      renderedNode + renderedChildren
    }
  }

  implicit class RenderableConfigTree(val c: Config) extends AnyVal {
    def render: String = "$root"
    def renderTree(depth: Int = 0): String = {
      val renderedNode = (" " * depth) + s"> ${c.render}\n"
      val renderedChildren = c.directives.map(d => d.renderTree(depth + 1)).mkString

      renderedNode + renderedChildren
    }
  }

  implicit class RenderableDirectiveTree(val d: Directive) extends AnyVal {
    def render: String = {
      val name = d.name
      val arguments = d.args.headOption
      .map(_ => " " + d.args.map { case (_, arg) => arg.render }.mkString(" "))
      .getOrElse("")
      val source = renderSource(d.source)

      name + arguments + source
    }

    def renderTree(depth: Int = 0): String = {
      val renderedNode = (" " * depth) + s"> ${d.render}\n"
      val renderedChildren = d.directives.map(d => d.renderTree(depth + 1)).mkString

      renderedNode + renderedChildren
    }
  }

  implicit class RenderableDefinitionTree(val d: Definition) extends AnyVal {
    def render: String = {
      val name = d.name
      val parameters = d.params.headOption
        .map(_ => " " + d.params.map(_.render).mkString(" "))
        .getOrElse("")
      val source = renderSource(d.source)

      name + parameters + source
    }

    def renderTree(depth: Int = 0): String = {
      val renderedNode = (" " * depth) + s"> ${d.render}\n"
      val renderedChildren = d.definitions.map(d => d.renderTree(depth + 1)).mkString

      renderedNode + renderedChildren
    }
  }
}
