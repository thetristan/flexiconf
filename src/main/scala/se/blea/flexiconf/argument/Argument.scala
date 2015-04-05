package se.blea.flexiconf.argument

/** Container for an argument value: name, value, and kind */
case class Argument(value: String,
                    kind: ArgumentKind[_] = StringArgument,
                    name: String = "?") {
  override def toString = s"$name:$kind<$value>"
}



