package se.blea.flexiconf.util

/** Container for the current node and all data associated with it */
object Stack {
  def apply[T](frames: T*): Stack[T] = Stack(List(frames:_*))
  def empty[T]: Stack[T] = Stack[T]()

  implicit def stack2List[T](stack: Stack[T]): List[T] = stack.frames
}


/** Carries context during the parse */
case class Stack[T](private var frames: List[T] = List.empty) {
  /** Push a frame on the stack */
  def push(frame: T): Unit = frames = frame :: frames

  /** Pop a frame off the stack */
  def pop(): T = {
    val head = frames.head
    frames = frames.tail
    head
  }

  /** Swap the top frame of the stack */
  def swap(frame: T): Unit = {
    pop()
    push(frame)
  }

  /** Get the top frame of the stack */
  def peek: Option[T] = frames.lift(0)
}
