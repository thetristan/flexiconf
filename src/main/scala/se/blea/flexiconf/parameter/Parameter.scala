package se.blea.flexiconf.parameter

import se.blea.flexiconf.argument.{ArgumentKind, StringArgument}

/** Container for defining the arguments a directive accepts: requires a name and type */
case class Parameter(name: String, kind: ArgumentKind[_] = StringArgument) {
  override def toString = s"$name:$kind"
}
