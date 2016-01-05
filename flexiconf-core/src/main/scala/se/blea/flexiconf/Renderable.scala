package se.blea.flexiconf

trait Renderable {
  /** Returns string representation of this **/
  def render: String
}

trait RenderableTree extends Renderable {
  /** Returns renderable children of this tree **/
  private[flexiconf] def children: List[RenderableTree]

  /** Returns string representation of this and its children **/
  private[flexiconf] def renderTree(depth: Int = 0): String

  /** Returns string representation of this and its children, including internal entities **/
  private[flexiconf] def renderDebugTree(depth: Int = 0): String
}
