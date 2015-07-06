package se.blea.flexiconf.parser

import se.blea.flexiconf.DefaultDefinition
import se.blea.flexiconf.parser.gen.ConfigParser.DirectiveListContext


/** Not all data can be carried on the stack, so we have an additional context object that carries this state */
private[flexiconf] case class ConfigVisitorContext(node: ConfigNode,
                                                   groups: Map[String, DirectiveListContext] = Map.empty,
                                                   directives: Set[DefaultDefinition] = Set.empty) {

  def withGroup(name: String, ds: DirectiveListContext): ConfigVisitorContext = {
    copy(groups = groups + (name -> ds))
  }

  def withDirective(d: DefaultDefinition): ConfigVisitorContext = {
    copy(directives = directives + d)
  }
}
