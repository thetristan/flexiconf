package se.blea.flexiconf.writer

import se.blea.flexiconf.Directive

object SerializableDirective {
  def serialize(d: Directive, depth: Int = 0): String = {
    val leader = "  " * depth

    val name = d.name

    val args = if (d.args.nonEmpty) {
      " " + d.args.map(_.originalValue).mkString(" ")
    } else {
      ""
    }

    val blockOrEnd = if (d.directives.nonEmpty) {
      " {\n" + d.directives.map(d => {
        val sameFile = d.source.map(_.sourceFile) == d.source.map(_.sourceFile)
        if (sameFile) {
          serialize(d, depth + 1)
        } else {
          // serialize later to new file
          s"include $path;"
        }

        }) + leader + "}\n"
    } else {
      ";\n"
    }

    leader + name + args + blockOrEnd
  }
}
