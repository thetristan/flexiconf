package se.blea.flexiconf.directive

/** Base trait for all directive flags */
trait DirectiveFlag {
}


object DirectiveFlags {
  object AllowOnce extends DirectiveFlag {
    override def toString: String = "once"
  }
}


/** Flags that affect how directives should be handled when creating the final configuration tree */
case class DirectiveFlags(flags: Set[DirectiveFlag] = Set.empty) {
  def allowOnce = flags.contains(DirectiveFlags.AllowOnce)

  override def toString: String = {
    flags.mkString("[", ",", "]")
  }
}
