package se.blea.flexiconf

trait Definition extends TraversableSchema with Warnable {
  def id: String
  def name: String
  def params: List[Param]
  def source: Option[Source]
  def flags: Set[DirectiveFlag]
  def documentation: String
}
