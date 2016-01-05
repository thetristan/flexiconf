package se.blea.flexiconf


/** Value trait for representing argument values **/
sealed trait ArgValue {
  def intValue: Option[Int]
  def longValue: Option[Long]
  def floatValue: Option[Float]
  def doubleValue: Option[Double]
  def stringValue: Option[String]
  def boolValue: Option[Boolean]

  /** Return this value, or the other value **/
  def or(other: ArgValue): ArgValue = OptionalValue(this, other)
  def |(other: ArgValue): ArgValue = or(other) // scalastyle:ignore method.name
}


/** Value class companion for implicit conversions **/
object ArgValue {
  implicit def argument2string(a: ArgValue): String = a.stringValue.getOrElse("")
  implicit def argument2bool(a: ArgValue): Boolean = a.boolValue.getOrElse(false)
  implicit def argument2int(a: ArgValue): Int = a.intValue.getOrElse(0)
  implicit def argument2long(a: ArgValue): Long = a.longValue.getOrElse(0)
  implicit def argument2float(a: ArgValue): Float = a.floatValue.getOrElse(0.0f)
  implicit def argument2double(a: ArgValue): Double = a.doubleValue.getOrElse(0.0)

  implicit def string2argumennt(v: String): ArgValue = StringValue(v)
  implicit def bool2argument(v: Boolean): ArgValue = BoolValue(v)
  implicit def int2argument(v: Int): ArgValue = LongValue(v)
  implicit def long2argument(v: Long): ArgValue = LongValue(v)
  implicit def float2argument(v: Float): ArgValue = DoubleValue(v)
  implicit def double2argument(v: Double): ArgValue = DoubleValue(v)
}


/** Value class for allowing defaults **/
case class OptionalValue(value: ArgValue, other: ArgValue) extends ArgValue {
  override def intValue: Option[Int] = value.intValue.orElse(other.intValue)
  override def doubleValue: Option[Double] = value.doubleValue.orElse(other.doubleValue)
  override def floatValue: Option[Float] = value.floatValue.orElse(other.floatValue)
  override def boolValue: Option[Boolean] = value.boolValue.orElse(other.boolValue)
  override def longValue: Option[Long] = value.longValue.orElse(other.longValue)
  override def stringValue: Option[String] = value.stringValue.orElse(other.stringValue)
  override def or(other2: ArgValue): ArgValue = OptionalValue(other, other2)
}


/** Value class implementation for implicit conversions **/
object NullValue extends ArgValue {
  override def intValue: Option[Int] = None
  override def doubleValue: Option[Double] = None
  override def floatValue: Option[Float] = None
  override def boolValue: Option[Boolean] = None
  override def longValue: Option[Long] = None
  override def stringValue: Option[String] = None
  override def or(other: ArgValue): ArgValue = other
}


/** Value class implementation for implicit conversions **/
case class BoolValue(value: Boolean) extends ArgValue {
  override def intValue: Option[Int] = None
  override def doubleValue: Option[Double] = None
  override def floatValue: Option[Float] = None
  override def boolValue: Option[Boolean] = Some(value)
  override def longValue: Option[Long] = None
  override def stringValue: Option[String] = Some(value.toString)
}


/** Value class implementation for implicit conversions **/
case class LongValue(value: Long) extends ArgValue {
  override def intValue: Option[Int] = Some(value.toInt)
  override def doubleValue: Option[Double] = Some(value.toDouble)
  override def floatValue: Option[Float] = Some(value.toFloat)
  override def boolValue: Option[Boolean] = None
  override def longValue: Option[Long] = Some(value.toLong)
  override def stringValue: Option[String] = Some(value.toString)
}


/** Value class implementation for implicit conversions **/
case class DoubleValue(value: Double) extends ArgValue {
  override def intValue: Option[Int] = Some(value.toInt)
  override def doubleValue: Option[Double] = Some(value.toDouble)
  override def floatValue: Option[Float] = Some(value.toFloat)
  override def boolValue: Option[Boolean] = None
  override def longValue: Option[Long] = Some(value.toLong)
  override def stringValue: Option[String] = Some(value.toString)
}


/** Value class implementation for implicit conversions **/
case class StringValue(value: String) extends ArgValue {
  override def intValue: Option[Int] = {
    if (DecimalType.accepts(value)) {
      Some(value.toInt)
    } else {
      None
    }
  }

  override def doubleValue: Option[Double] = {
    if (DecimalType.accepts(value)) {
      Some(value.toDouble)
    } else {
      None
    }
  }

  override def floatValue: Option[Float] = {
    if (DecimalType.accepts(value)) {
      Some(value.toFloat)
    } else {
      None
    }
  }

  override def boolValue: Option[Boolean] = {
    if (BoolType.accepts(value)) {
      Some(value.toBoolean)
    } else {
      None
    }
  }

  override def longValue: Option[Long] = {
    if (DecimalType.accepts(value)) {
      Some(value.toLong)
    } else {
      None
    }
  }

  override def stringValue: Option[String] = Some(value)
}
