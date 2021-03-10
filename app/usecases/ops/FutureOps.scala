package usecases.ops

import usecases.{SystemError, UseCaseError}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait FutureOps {
  implicit class FutureTOps[T](future: Future[T])(
      implicit ec: ExecutionContext) {
    def ifFailThenToUseCaseError(message: String): Future[T] =
      future.transformWith {
        case Success(v) => Future.successful(v)
        case Failure(exception) =>
          Future.failed(SystemError(message + exception.getMessage))
      }
  }
}
