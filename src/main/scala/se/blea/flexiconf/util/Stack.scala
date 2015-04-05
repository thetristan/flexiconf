package se.blea.flexiconf.util

/** Container for the current node and all data associated with it */
object Stack {
  def empty[T] = Stack[T]()
}


/** Carries context during the parse */
case class Stack[T](private var frames: List[T] = List.empty) {

  /** Push the provided node on the stack and return the evaluated expression */
  def enterFrame[A](frame: T)(withFrame: => A): A = {
    push(frame)
    val result = withFrame
    pop()

    result
  }

  /** Push a frame on the stack */
  def push(frame: T) = frames = frame :: frames

  /** Pop a frame off the stack */
  def pop() = frames = frames.tail

  /** Get the top frame of the stack */
  def peek: Option[T] = frames.lift(0)

   /** Find a frame **/
  def find(p: (T) => Boolean) = frames.find(p)

  /** Filter frames **/
  def filter(p: (T) => Boolean) = frames.filter(p)

  /** Return string representation of the stack for debugging */
  def render = frames.mkString("* ", "\n* ", "\n")
}
