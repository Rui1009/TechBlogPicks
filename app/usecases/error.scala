package usecases

import domains.DomainError
import infra.InfraError

sealed trait UseCaseError {
  val errorMessage: String
}

final case class SystemError(error: InfraError, operation: String)
    extends UseCaseError {
  override val errorMessage: String = s"""
                                         |${operation}
                                         |${error.errorMessage}
     """.stripMargin
}

final case class BadParamsError(error: DomainError, operation: String)
    extends UseCaseError {
  override val errorMessage: String = s"""
                                         |${operation}
                                         |${error.errorMessage}
     """.stripMargin
}

final case class NotFoundError(operation: String) extends UseCaseError {
  override val errorMessage: String = operation
}
