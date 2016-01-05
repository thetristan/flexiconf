package se.blea.flexiconf

import se.blea.flexiconf.parser._

case class DefaultSchema(definitions: Set[Definition]) extends Schema {
  override def warnings: List[String] = List.empty
}

object DefaultSchema {
  def fromNode(node: Node): DefaultSchema = {
    val definitions: Set[Definition] = node match {
      case TreeNode(t) =>
        t.nodes.map(_.flatMap(DefaultDefinition.fromNode).toSet[Definition]).getOrElse(Set.empty)
      case _ => Set.empty
    }

    DefaultSchema(definitions)
  }
}
