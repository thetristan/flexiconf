package se.blea.flexiconf

import scala.collection.immutable.ListMap


/** Public interface for consuming configuration */
trait Directive extends TraversableConfig with Warnable {
  /** Returns name of the directive **/
  def name: String

  /** Returns all args **/
  def args: ListMap[String, Arg]

  /** Returns where this directive was defined **/
  def source: Option[Source]

  /** Return the first value */
  def apply: ArgValue

  /** Return the first value, or the other value **/
  def or(other: ArgValue): ArgValue = apply | other
  def |(other: ArgValue): ArgValue = or(other) // scalastyle:ignore method.name

  /** Returns whether an argument is allowed and contained for this directive **/
  def allowsArg(name: String): Boolean
  def containsArg(name: String): Boolean

  /** Find an argument by name **/
  def argValue(name: String): ArgValue

  /** Find an argument by index **/
  def argValue(index: Int): ArgValue

  /** Find multiple arguments by name **/
  def argValue(names: (String, String)): (ArgValue, ArgValue) =
    (argValue(names._1), argValue(names._2))

  def argValue(names: (String, String, String)): (ArgValue, ArgValue, ArgValue) =
    (argValue(names._1), argValue(names._2), argValue(names._3))

  def argValue(names: (String, String, String, String)): (ArgValue, ArgValue, ArgValue, ArgValue) =
    (argValue(names._1), argValue(names._2), argValue(names._3), argValue(names._4))

  def argValue(names: (String, String, String, String, String)): (ArgValue, ArgValue, ArgValue, ArgValue, ArgValue) =
    (argValue(names._1), argValue(names._2), argValue(names._3), argValue(names._4), argValue(names._5))

  def argValue(names: (String, String, String, String, String, String)):
    (ArgValue, ArgValue, ArgValue, ArgValue, ArgValue, ArgValue) =
      (argValue(names._1), argValue(names._2), argValue(names._3), argValue(names._4), argValue(names._5), argValue(names._6))

  /** Operators for alternative traversal of configuration by name **/
  // scalastyle:off method.name
  def %(name: String): ArgValue = argValue(name)
  def %(names: (String, String)): (ArgValue, ArgValue) = argValue(names)
  def %(names: (String, String, String)): (ArgValue, ArgValue, ArgValue) = argValue(names)
  def %(names: (String, String, String, String)): (ArgValue, ArgValue, ArgValue, ArgValue) = argValue(names)
  def %(names: (String, String, String, String, String)): (ArgValue, ArgValue, ArgValue, ArgValue, ArgValue) = argValue(names)
  def %(names: (String, String, String, String, String, String)):
    (ArgValue, ArgValue, ArgValue, ArgValue, ArgValue, ArgValue) = argValue(names)
  // scalastyle:on method.name
}


object Directive {
  /** Implicit conversions **/
  implicit def directive2argumentValue(d: Directive): ArgValue = d.apply
  implicit def directive2string(d: Directive): String = d.apply
  implicit def directive2bool(d: Directive): Boolean = d.apply
  implicit def directive2int(d: Directive): Int = d.apply
  implicit def directive2long(d: Directive): Long = d.apply
  implicit def directive2float(d: Directive): Float = d.apply
  implicit def directive2double(d: Directive): Double = d.apply
}




















