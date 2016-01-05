package se.blea.flexiconf

/** Options for configuration parsing */
case class ConfigOptions(allowUnknownDirectives: Boolean = false,
                         allowDuplicateDirectives: Boolean = false,
                         allowMissingGroups: Boolean = false,
                         allowMissingIncludes: Boolean = false,
                         allowIncludeCycles: Boolean = false)
