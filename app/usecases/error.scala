package usecases

sealed abstract class UseCaseError(message: String) extends Exception {
  val errorMessage: String = s"""
                       |${this.getClass.getSimpleName}
                       |${message}
     """.stripMargin
}

final case class SystemError(message: String) extends UseCaseError(message)

final case class BadParamsError(message: String) extends UseCaseError(message)

final case class NotFoundError(message: String) extends UseCaseError(message)
