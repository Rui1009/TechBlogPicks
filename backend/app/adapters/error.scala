package adapters

import usecases.{BadParamsError, NotFoundError => UNotFoundError, SystemError, UseCaseError}

sealed abstract class AdapterError(message: String) extends Exception {
  override def getMessage: String = s"""${this.getClass.getSimpleName}
                                       |$message
                                       |""".stripMargin
}

final case class BadRequestError(message: String) extends AdapterError(message)

final case class InternalServerError(message: String)
    extends AdapterError(message)

final case class NotFoundError(message: String) extends AdapterError(message)

object AdapterError {
  def fromUseCaseError(message: String, error: UseCaseError): AdapterError =
    error match {
      case e: SystemError    => InternalServerError(message + "\n" + e.getMessage)
      case e: UNotFoundError => NotFoundError(message + "\n" + e.getMessage)
      case e: BadParamsError => BadRequestError(message + "\n" + e.getMessage)
    }
}
