package se.blea.flexiconf

/** Container for defining the arguments a directive accepts: requires a name and type */
case class Parameter(name: String, kind: ArgumentKind[_] = StringArgument) {
  override def toString: String = s"$name:$kind"
}
