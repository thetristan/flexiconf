package se.blea.flexiconf.docgen

import se.blea.flexiconf.Schema

trait DocGenerator {
  def process(schema: Schema): String
}


