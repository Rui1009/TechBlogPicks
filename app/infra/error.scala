package infra

sealed abstract class InfraError(message: String) extends Exception {
  override def getMessage: String = s"""
                                |${this.getClass.getSimpleName}
                                |${message}""".stripMargin
}

final case class DBError(message: String) extends InfraError(message)
