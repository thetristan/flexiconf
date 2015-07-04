package se.blea.flexiconf


/** Container for source information, including file, line and position */
case class Source(sourceFile: String,
                  line: Long,
                  charPosInLine: Long) {
  override def toString = s"$sourceFile:$line:$charPosInLine"
}
