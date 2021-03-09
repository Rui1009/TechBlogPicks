package usecases.ops

import infra.InfraError
import usecases.SystemError

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait FutureOps {
  implicit class InfraErrorOpe[E <: InfraError, R](
      futureEither: Future[Either[E, R]]
  )(implicit val ec: ExecutionContext) {
    def ifLeftThenToUseCaseError(
        message: String): Future[Either[SystemError, R]] =
      futureEither.transformWith {
        case Success(Right(v)) => Future.successful(Right(v))
        case Success(Left(e)) =>
          Future.successful(Left(SystemError(message + "\n" + e.errorMessage)))
        case Failure(exception) =>
          Future.failed(SystemError(message + "\n" + exception.getMessage))
      }
  }
}
