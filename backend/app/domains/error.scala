package domains

sealed trait DomainError extends Throwable {
  val content: String
  val errorMessage: String = s"""${this.getClass.getName}: $content
       |""".stripMargin
}

final case class EmptyStringError(className: String) extends DomainError {
  override lazy val content: String = s"$className is empty string"
}

final case class NegativeNumberError(className: String) extends DomainError {
  override lazy val content: String = s"$className is negative number"
}

final case class RegexError(className: String) extends DomainError {
  override lazy val content: String = s"$className don't match pattern"
}

final case class NotExistError(className: String) extends DomainError {
  override lazy val content: String = s"$className don't exist"
}
