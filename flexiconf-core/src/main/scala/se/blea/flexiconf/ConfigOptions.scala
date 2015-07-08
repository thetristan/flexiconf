package se.blea.flexiconf

import java.io.InputStream

import se.blea.flexiconf.parser.ConfigVisitorOptions

/** Options for configuration parsing */
case class ConfigOptions private (private[flexiconf] val sourceFile: String = "",
                                  private[flexiconf] val inputStream: Option[InputStream] = None) {
  if (sourceFile.isEmpty && inputStream.isEmpty) {
    throw new IllegalStateException("A source file or valid input stream must be supplied")
  }

  private[flexiconf] var visitorOpts = ConfigVisitorOptions(sourceFile)

  def ignoreDuplicateDirectives: ConfigOptions = {
    visitorOpts = visitorOpts.copy(allowDuplicateDirectives = true)
    this
  }

  def ignoreUnknownDirectives: ConfigOptions = {
    visitorOpts = visitorOpts.copy(allowUnknownDirectives = true)
    this
  }

  def ignoreMissingGroups: ConfigOptions = {
    visitorOpts = visitorOpts.copy(allowMissingGroups = true)
    this
  }

  def ignoreMissingIncludes: ConfigOptions = {
    visitorOpts = visitorOpts.copy(allowMissingIncludes = true)
    this
  }

  def ignoreIncludeCycles: ConfigOptions = {
    visitorOpts = visitorOpts.copy(allowIncludeCycles = true)
    this
  }

  def withDirectives(ds: Set[DefaultDefinition]): ConfigOptions = {
    visitorOpts = visitorOpts.copy(directives = visitorOpts.directives ++ ds)
    this
  }

  def withDirectives(d: DefaultDefinition*): ConfigOptions = {
    visitorOpts = visitorOpts.copy(directives = visitorOpts.directives ++ d)
    this
  }

  def withSchema(s: Schema) = {
    visitorOpts = visitorOpts.copy(directives = visitorOpts.directives ++ s.directives)
    this
  }
}

object ConfigOptions {
  def withSourceFile(sourceFile: String) = ConfigOptions(sourceFile)
  def withInputStream(streamName: String, inputStream: InputStream) = ConfigOptions(streamName, Option(inputStream))
}



