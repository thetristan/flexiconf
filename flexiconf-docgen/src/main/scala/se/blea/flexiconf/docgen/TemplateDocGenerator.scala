package se.blea.flexiconf.docgen

import java.io.StringWriter
import java.util

import org.pegdown.PegDownProcessor
import se.blea.flexiconf.parser.Definition

import scala.collection.JavaConversions._

import com.github.mustachejava.DefaultMustacheFactory
import se.blea.flexiconf._


class TemplateDocGenerator(templatePath: String) extends DocGenerator {
  lazy val processor = new PegDownProcessor()
  lazy val mf = new DefaultMustacheFactory()

  private def presentNode(d: DefaultDefinition): java.util.Map[String, Any] = {
    val name = d.name
    val arity = d.parameters.size
    val params = d.parameters.map(presentNodeParams).mkString(" ")
    val blockFlag = if (d.children.nonEmpty) { "*" } else { "" }
    val id = s"$name/$arity$blockFlag"

    Map(
      "name" -> name,
      "id" -> id,
      "arity" -> arity,
      "syntax" -> s"$name $params",
      "notes" -> processor.markdownToHtml(d.documentation),
      "flags" -> d.flags.map(_.documentation),
      "directives" -> new util.ArrayList(d.children.map(presentNode))
    )
  }

  private def presentNodeParams(param: Parameter) = s"${param.name}:${param.kind}"

  override def process(schema: Schema): String = {
    val w = new StringWriter()
    val ctx = new util.HashMap[String, Object](Map(
      "directives" -> new util.ArrayList(schema.directives.map(presentNode))
    ))

    mf.compile(templatePath).execute(w, ctx)

    w.toString
  }
}
