package se.blea.flexiconf.docgen

import java.io.StringWriter
import java.util

import org.pegdown.PegDownProcessor

import scala.collection.JavaConversions._

import com.github.mustachejava.DefaultMustacheFactory
import se.blea.flexiconf._


class TemplateDocGenerator(templatePath: String) extends DocGenerator {
  lazy val processor = new PegDownProcessor()
  lazy val mf = new DefaultMustacheFactory()

  private def presentNode(d: Definition): java.util.Map[String, Any] = {
    val name = d.name
    val arity = d.params.size
    val params = d.params.map(presentNodeParams).mkString(" ")
    val blockFlag = if (d.definitions.nonEmpty) { "*" } else { "" }
    val id = s"$name/$arity$blockFlag"

    Map(
      "name" -> name,
      "id" -> id,
      "arity" -> arity,
      "syntax" -> s"$name $params",
      "notes" -> processor.markdownToHtml(d.documentation),
      "flags" -> d.flags.map(_.documentation),
      "directives" -> new util.ArrayList(d.definitions.map(presentNode))
    )
  }

  private def presentNodeParams(param: Param) = s"${param.name}:${param.kind}"

  override def process(schema: Schema): String = {
    val w = new StringWriter()
    val ctx = new util.HashMap[String, Object](Map(
      "directives" -> new util.ArrayList(schema.definitions.map(presentNode))
    ))

    mf.compile(templatePath).execute(w, ctx)

    w.toString
  }
}
