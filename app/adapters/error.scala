package adapters

import usecases.{
  BadParamsError,
  SystemError,
  UseCaseError,
  NotFoundError => UNotFoundError
}

sealed abstract class AdapterError(message: String) extends Exception {
  override def getMessage: String = s"${this.getClass.getSimpleName}$message"
}

final case class BadRequestError(message: String) extends AdapterError(message)

final case class InternalServerError(message: String)
    extends AdapterError(message)

final case class NotFoundError(message: String) extends AdapterError(message)

object AdapterError {
  def fromUseCaseError(message: String, error: UseCaseError): AdapterError =
    error match {
      case e: SystemError    => InternalServerError("\n" + message + e.getMessage)
      case e: UNotFoundError => NotFoundError("\n" + message + e.getMessage)
      case e: BadParamsError => BadRequestError("\n" + message + e.getMessage)
    }
}
