package se.blea.flexiconf.util

/**
 * Created by tblease on 7/5/15.
 */
object ExceptionUtil {
  type IllegalStateExceptionGenerator = (String, Set[String], Set[String]) => Throwable

  def entityNotAllowed(singular: String, plural: String, warning: String, complement: String): IllegalStateExceptionGenerator = {
    (name: String, allowedDirectives: Set[String], illegalDirectives: Set[String]) => {
      val illegal = illegalDirectives.mkString("', '")
      val subject = if (illegalDirectives.size > 1) plural else singular

      val allowedMessage = if (allowedDirectives.size > 0) {
        val allowed = allowedDirectives.mkString("', '")
        val verb = if (allowedDirectives.size > 1) "are" else "is"
        s": only '$allowed' $verb $complement"
      } else {
        ""
      }

      new IllegalStateException(s"$subject '$illegal' $warning $name" ++ allowedMessage)
    }
  }
}
