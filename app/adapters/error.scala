package adapters

import usecases.{
  BadParamsError,
  SystemError,
  UseCaseError,
  NotFoundError => UNotFoundError
}

sealed abstract class AdapterError(message: String) {
  val errorMessage: String = s"""
                                |${this.getClass.getSimpleName}
                                |$message""".stripMargin
}

final case class BadRequestError(message: String) extends AdapterError(message)

final case class InternalServerError(message: String)
    extends AdapterError(message)

final case class NotFoundError(message: String) extends AdapterError(message)

object AdapterError {
  def fromUseCaseError(error: UseCaseError): AdapterError = error match {
    case e: SystemError    => InternalServerError(e.getMessage)
    case e: UNotFoundError => NotFoundError(e.getMessage)
    case e: BadParamsError => BadRequestError(e.getMessage)
  }
}
