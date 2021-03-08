package domains

sealed trait DomainError {
  val errorMessage: String
}

final case class EmptyStringError(className: String) extends DomainError {
  override val errorMessage: String = s"${className} is empty string"
}

final case class NegativeNumberError(className: String) extends DomainError {
  override val errorMessage: String = s"${className} is negative number"
}
