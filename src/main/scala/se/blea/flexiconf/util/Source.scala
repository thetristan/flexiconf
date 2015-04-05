package se.blea.flexiconf.util

import org.antlr.v4.runtime.ParserRuleContext

case class Source(sourceFile: String, line: Long, charPosInLine: Long) {
  override def toString = s"$sourceFile:$line:$charPosInLine"
}

object Source {
  /** Returns a new Source object based on the provided context */
  def fromContext(sourceFile: String, ctx: ParserRuleContext): Source = {
    Source(sourceFile, ctx.getStart.getLine, ctx.getStart.getCharPositionInLine)
  }
}
