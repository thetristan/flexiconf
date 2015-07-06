package se.blea.flexiconf

case class Schema(name: String,
                  source: Source,
                  directives: Set[DefaultDefinition])
