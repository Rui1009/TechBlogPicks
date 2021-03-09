package usecases.ops

import infra.InfraError
import usecases.{SystemError, UseCaseError}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait ErrorOps {
  implicit class InfraErrorOpe[E <: InfraError, R](
      futureEither: Future[Either[E, R]]
  )(implicit val ec: ExecutionContext) {
    def ifLeftThenToUseCaseError(
        message: String): Future[Either[UseCaseError, R]] =
      futureEither.transformWith {
        case Success(Right(v)) => Future.successful(Right(v))
        case Success(Left(e)) =>
          Future.successful(Left(SystemError(message + "\n" + e.getMessage)))
        case Failure(e) =>
          Future.failed(SystemError(message + "\n" + e.getMessage))
      }
  }
}
