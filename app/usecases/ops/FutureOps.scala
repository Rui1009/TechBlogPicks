package usecases.ops

import usecases.SystemError

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait FutureOps {
  implicit class FutureOps[T](future: Future[T])(
      implicit ec: ExecutionContext) {
    def ifFailThenToUseCaseError(message: String): Future[T] =
      future.transformWith {
        case Success(v) => Future.successful(v)
        case Failure(exception) =>
          Future.failed(SystemError(message + "\n" + exception.getMessage))
      }
  }
}
