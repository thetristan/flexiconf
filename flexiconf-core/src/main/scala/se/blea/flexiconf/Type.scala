package se.blea.flexiconf

sealed trait Arg {
  /** Original value of the argument */
  def value: String
  /** Kind of argument */
  def kind: Type
  /** ArgValue representation of this value based on the kind */
  def argValue: ArgValue = kind.valueOf(value)
}

object Arg {
  def fromType(value: String, t: Type): Arg = t match {
    case BoolType => BoolArg(value)
    case DecimalType => DecimalArg(value)
    case DurationType => DurationArg(value)
    case IntType => IntArg(value)
    case PercentageType => PercentageArg(value)
    case StringType => StringArg(value)
  }
}

case class StringArg(value: String) extends Arg { val kind = StringType }
case class IntArg(value: String) extends Arg { val kind = IntType }
case class DecimalArg(value: String) extends Arg { val kind = DecimalType }
case class DurationArg(value: String) extends Arg { val kind = DurationType }
case class PercentageArg(value: String) extends Arg { val kind = PercentageType }
case class BoolArg(value: String) extends Arg { val kind = BoolType }

sealed trait Param {
  /** Name of this parameter */
  def name: String
  /** Kind of parameter */
  def kind: Type
}


object Param {
  def fromType(name: String, t: Type): Param = t match {
    case BoolType => BoolParam(name)
    case DecimalType => DecimalParam(name)
    case DurationType => DurationParam(name)
    case IntType => IntParam(name)
    case PercentageType => PercentageParam(name)
    case StringType => StringParam(name)
  }
}

case class StringParam(name: String) extends Param { val kind = StringType }
case class IntParam(name: String) extends Param { val kind = IntType }
case class DecimalParam(name: String) extends Param { val kind = DecimalType }
case class DurationParam(name: String) extends Param { val kind = DurationType }
case class PercentageParam(name: String) extends Param { val kind = PercentageType }
case class BoolParam(name: String) extends Param { val kind = BoolType }

/** Base trait for argument types */
sealed trait Type {
  /** Returns true if the value meets the criteria for this type */
  def accepts(value: String): Boolean

  /** Returns the native value for the provided value */
  def valueOf(value: String): ArgValue
}

object Type {
  def fromString(name: String): Type = name match {
    case "Bool" => BoolType
    case "Decimal" => DecimalType
    case "Duration" => DurationType
    case "Int" => IntType
    case "Percentage" => PercentageType
    case "String" => StringType
  }
}

/** Boolean values */
case object BoolType extends Type {
  val boolTruePattern = "on|yes|y|true"
  val boolFalsePattern = "off|no|n|false"
  val boolPattern = boolTruePattern ++ "|" ++ boolFalsePattern

  override def accepts(value: String): Boolean = value.toLowerCase matches boolPattern
  override def valueOf(value: String): ArgValue = BoolValue(value.toLowerCase matches boolTruePattern)
  override def toString: String = "Bool"
}


/** Integer values */
case object IntType extends Type {
  val intPattern = "(-?(?:0|[1-9]\\d*))"

  override def accepts(value: String): Boolean = value matches intPattern
  override def valueOf(value: String): ArgValue = LongValue(value.toLong)
  override def toString: String = "Int"
}


/** Decimal values */
case object DecimalType extends Type {
  val decimalPattern = "(-?(?:0|[1-9]\\d*))(\\.\\d+)?"

  override def accepts(value: String): Boolean = value matches decimalPattern
  override def valueOf(value: String): ArgValue = DoubleValue(value.toDouble)
  override def toString: String = "Decimal"
}


/** Duration values */
case object DurationType extends Type {
  val durationPattern = "(-?(?:0|[1-9]\\d*)(?:\\.\\d+)?)(ms|s|m|h|d|w|M|y)".r
  val multipliers = Map(
    "ms" -> 1L,
    "s"  -> 1000L,
    "m"  -> 60000L,
    "h"  -> 3600000L,
    "d"  -> 86400000L,
    "w"  -> 604800000L,
    "M"  -> 26297460000L,
    "y"  -> 315569520000L)

  override def accepts(value: String): Boolean = durationPattern.pattern.matcher(value).matches
  override def valueOf(value: String): ArgValue = value match {
    case durationPattern(amount, unit) => LongValue((amount.toDouble * multipliers.getOrElse(unit, 1L)).toLong)
    case _ => throw new IllegalStateException(s"Can't get duration value from $value")
  }
  override def toString: String = "Duration"
}


/** Percentage values */
case object PercentageType extends Type {
  val percentagePattern = "(-?(?:0|[1-9]\\d*)(?:\\.\\d+)?)%".r

  override def accepts(value: String): Boolean = percentagePattern.pattern.matcher(value).matches
  override def valueOf(value: String): ArgValue = value match {
    case percentagePattern(amount) => DoubleValue(amount.toDouble / 100)
    case _ => throw new IllegalStateException(s"Can't get percentage value from $value")
  }
  override def toString: String = "Percentage"
}


/** String values */
case object StringType extends Type {
  override def accepts(value: String): Boolean = true
  override def valueOf(value: String): ArgValue = StringValue(value)
  override def toString: String = "String"
}
