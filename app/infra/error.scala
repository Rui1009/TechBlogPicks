package infra

sealed trait InfraError {
  val errorMessage: String
}

final case class DBError(message: String, operation: String)
    extends InfraError {
  override val errorMessage: String =
    s"""
       |${operation}
       |detail: ${message}
     """.stripMargin
}
