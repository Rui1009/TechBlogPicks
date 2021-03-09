package domains

sealed trait DomainError {
  val content: String
  val errorMessage: String = s"${this.getClass.getName}: $content"
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
