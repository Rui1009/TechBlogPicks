package usecases.ops

import usecases.{NotFoundError, SystemError}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait FutureOps {
  implicit class FutureTOps[T](future: Future[T])(implicit
    ec: ExecutionContext
  ) {
    def ifFailThenToUseCaseError(message: String): Future[T] =
      future.transformWith {
        case Success(v)         => Future.successful(v)
        case Failure(exception) =>
          Future.failed(SystemError(message + "\n" + exception.getMessage))
      }
  }

  implicit class FutureOptOps[T](futureOpt: Future[Option[T]])(implicit
    ec: ExecutionContext
  ) {
    def ifNotExistsToUseCaseError(message: String): Future[T] =
      futureOpt.transformWith {
        case Success(Some(value)) => Future.successful(value)
        case Success(None)        => Future.failed(NotFoundError(message))
        case Failure(exception)   =>
          Future.failed(SystemError(message + exception.getMessage))
      }
  }

  implicit class FutureSeqOps[T](futureSeq: Future[Seq[T]])(implicit
    ec: ExecutionContext
  ) {
    def ifNotExistsToUseCaseError(message: String): Future[Seq[T]] =
      futureSeq.transformWith {
        case Success(v) if v.nonEmpty => Future.successful(v)
        case Success(v) if v.isEmpty  => Future.failed(NotFoundError(message))
        case Failure(exception)       =>
          Future.failed(SystemError(message + "\n" + exception.getMessage))
      }
  }
}
