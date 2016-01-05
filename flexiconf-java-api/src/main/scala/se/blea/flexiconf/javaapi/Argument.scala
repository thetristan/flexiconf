package se.blea.flexiconf.javaapi

import se.blea.flexiconf._


/** Java-friendly wrapper for the Argument API */
class Argument(private val _name: String,
               private val _arg: se.blea.flexiconf.Arg) {
  def getName: String = _name
  def getKind: ArgumentKind = _arg.kind match {
    case StringType => ArgumentKind.String
    case IntType => ArgumentKind.Int
    case BoolType => ArgumentKind.Bool
    case DecimalType => ArgumentKind.Decimal
    case DurationType => ArgumentKind.Duration
    case PercentageType => ArgumentKind.Percentage
    case _ => throw new IllegalArgumentException("Unknown argument kind")
  }
}
