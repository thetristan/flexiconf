package se.blea.flexiconf

import java.io.InputStream

import se.blea.flexiconf.parser.SchemaVisitorOptions

/** Options for parsing schemas */
case class SchemaOptions private (private[flexiconf] val sourceFile: String = "",
                                  private[flexiconf] val inputStream: Option[InputStream] = None) {
  if (sourceFile.isEmpty && inputStream.isEmpty) {
    throw new IllegalStateException("A source file or valid input stream must be supplied")
  }

  private[flexiconf] var visitorOpts = SchemaVisitorOptions(sourceFile)
}


object SchemaOptions {
  def withSourceFile(sourceFile: String) = SchemaOptions(sourceFile)
  def withInputStream(streamName: String, inputStream: InputStream) = SchemaOptions(streamName, Option(inputStream))
}
