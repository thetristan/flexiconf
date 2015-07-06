package se.blea.flexiconf.writer

/**
 * Created by tblease on 7/5/15.
 */
trait SerializableConfig {
  def serialize(indent: Int = 0): String
}
