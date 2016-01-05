package se.blea.flexiconf.parser

import se.blea.flexiconf.{Type, Source}

sealed trait Node {
  def source: Source
}

sealed trait TreeNode {
  def nodes: Option[List[Node]]
}

object TreeNode {
  def unapply(n: Node): Option[TreeNode] = n match {
    case t: TreeNode => Some(t)
    case _ => None
  }
}

sealed trait BuiltInNode extends Node
sealed trait BuiltInTreeNode extends BuiltInNode with TreeNode

sealed trait UserDefinedNode extends Node
sealed trait UserDefinedTreeNode extends UserDefinedNode with TreeNode

case class ArgNode(
  source: Source,
  value: String,
  kind: Type
) extends UserDefinedNode

case class ParameterNode(
  source: Source,
  name: String,
  kind: Type
) extends UserDefinedNode

case class FlagNode(
  source: Source,
  name: String,
  value: Option[String] = None
) extends UserDefinedNode

case class DefinitionNode(
  source: Source,
  name: String,
  parameters: List[ParameterNode],
  flags: Set[FlagNode],
  documentation: String,
  nodes: Option[List[Node]] = None
) extends UserDefinedTreeNode

case class DirectiveNode(
  source: Source,
  name: String,
  arguments: List[ArgNode],
  nodes: Option[List[Node]] = None
) extends UserDefinedTreeNode

case class GroupNode(
  source: Source,
  name: String,
  nodes: Option[List[Node]] = None
) extends BuiltInTreeNode

case class WarningNode(
  source: Source,
  reason: String
) extends BuiltInNode

case class UseNode(
  source: Source,
  name: String,
  nodes: Option[List[Node]] = None
) extends BuiltInTreeNode

case class IncludeNode(
  source: Source,
  path: String,
  nodes: Option[List[Node]] = None
) extends BuiltInTreeNode

case class RootNode(
  source: Source,
  nodes: Option[List[Node]] = None
) extends BuiltInTreeNode
