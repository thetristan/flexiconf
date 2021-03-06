package se.blea.flexiconf

import org.antlr.v4.runtime.ParserRuleContext
import se.blea.flexiconf.parser.gen.SchemaParser._
import se.blea.flexiconf.parser.gen.SchemaParserBaseVisitor

import scala.collection.JavaConversions._

/** Container for defining the arguments a directive accepts: requires a name and type */
case class Parameter(name: String, kind: ArgumentKind[_] = StringArgument) {
  override def toString: String = s"$name:$kind"
}

/** Returns a Parameter for a given single or list of ParameterContext */
private[flexiconf] object ParameterVisitor extends SchemaParserBaseVisitor[Parameter] {
  def apply(ctx: ParserRuleContext): List[Parameter] = ctx match {
    case paramList: ParameterListContext => (paramList.parameter map visitParameter).toList
    case param: ParameterContext => List(visitParameter(param))
    case _ => List.empty
  }

  override def visitParameter(ctx: ParameterContext): Parameter = {
    val name = ctx.parameterName().getText
    val kind = ctx.parameterValue().getText

    ParameterTypeVisitor.visitParameterValue(ctx.parameterValue()) match {
      case IntArgument => Parameter(name, IntArgument)
      case StringArgument => Parameter(name, StringArgument)
      case DecimalArgument => Parameter(name, DecimalArgument)
      case DurationArgument => Parameter(name, DurationArgument)
      case PercentageArgument => Parameter(name, PercentageArgument)
      case BoolArgument => Parameter(name, BoolArgument)
      case _ => Parameter(name, UnknownArgument)
    }
  }
}
