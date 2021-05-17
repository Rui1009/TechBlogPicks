package usecases.ops

import domains.DomainError
import usecases.NotFoundError

import scala.concurrent.Future

trait EitherOps {
  implicit final class EitherTOps[S <: DomainError, T](
    private val either: Either[S, T]
  ) {
    def ifLeftThenToUseCaseError(message: String): Future[T] = either match {
      case Left(e)  =>
        Future.failed(NotFoundError(message + "\n" + e.errorMessage))
      case Right(v) => Future.successful(v)
    }
  }
}
