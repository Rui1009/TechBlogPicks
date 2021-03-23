package usecases.ops

import usecases.SystemError

import scala.concurrent.{ExecutionContext, Future}

trait OptionOps {
  implicit class OptionTOps[T](private val option: Option[T]) {
    def ifNotExistsToUseCaseError(message: String): Future[T] = option match {
      case Some(v) => Future.successful(v)
      case None    => Future.failed(SystemError(message))
    }
  }
}
