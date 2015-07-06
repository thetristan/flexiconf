package se.blea.flexiconf.parser

import se.blea.flexiconf.DefaultDefinition

/** Options for the ConfigVisitor */
private[flexiconf] case class ConfigVisitorOptions(sourceFile: String,
                                                   allowUnknownDirectives: Boolean = false,
                                                   allowDuplicateDirectives: Boolean = false,
                                                   allowMissingGroups: Boolean = false,
                                                   allowMissingIncludes: Boolean = false,
                                                   allowIncludeCycles: Boolean = false,
                                                   directives: Set[DefaultDefinition] = Set.empty)
