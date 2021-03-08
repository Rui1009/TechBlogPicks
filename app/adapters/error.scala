package adapters

import usecases.{
  BadParamsError,
  SystemError,
  UseCaseError,
  NotFoundError => UNotFoundError
}

sealed trait AdapterError {
  val errorMessage: String
}

final case class BadRequestError(error: Option[UseCaseError])
    extends AdapterError {
  override val errorMessage: String = s"""
                                         |${this.getClass.getSimpleName
                                           .dropRight(1)}
                                         |${error match {
                                           case Some(e) => e.errorMessage
                                           case _       => ""
                                         }}
                                         |""".stripMargin
}

final case class InternalServerError(error: UseCaseError) extends AdapterError {
  override val errorMessage: String = s"""
                                         |${this.getClass.getSimpleName
                                           .dropRight(1)}
                                         |${error.errorMessage}
                                         |""".stripMargin
}

final case class NotFoundError(error: UseCaseError) extends AdapterError {
  override val errorMessage: String = s"""
                                         |${this.getClass.getSimpleName
                                           .dropRight(1)}
                                         |${error.errorMessage}
                                         |""".stripMargin
}

object AdapterError {
  def create(error: UseCaseError): Unit = error match {
    case e if SystemError    => InternalServerError(e)
    case e if UNotFoundError => NotFoundError(e)
    case e if BadParamsError => BadRequestError(Some(e))
  }
}
