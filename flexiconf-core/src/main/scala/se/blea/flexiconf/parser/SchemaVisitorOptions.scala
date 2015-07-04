package se.blea.flexiconf.parser

/** Options for SchemaVisitors */
case class SchemaVisitorOptions(sourceFile: String,
                                allowMissingGroups: Boolean = false)
