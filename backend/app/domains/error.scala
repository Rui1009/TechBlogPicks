package domains

sealed trait DomainError {
  val content: String
  val errorMessage: String = s"""${this.getClass.getSimpleName}: $content
       |""".stripMargin
}
object DomainError       {
  def combine(errors: Seq[DomainError]): DomainError = new DomainError {
    override lazy val content: String = errors.map(_.errorMessage).mkString
    override val errorMessage: String = content
  }
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
