package infra

sealed abstract class InfraError(message: String) {
  val errorMessage: String = s"""
                                |${this.getClass.getSimpleName}
                                |${message}
     """.stripMargin
}

final case class DBError(message: String) extends InfraError(message)
