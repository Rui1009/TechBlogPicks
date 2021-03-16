package infra.syntax

import infra.{APIError, DBError}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object future extends FutureSyntax

trait FutureSyntax {
  implicit final def infraSyntaxFuture[T](future: Future[T])(implicit
    ec: ExecutionContext
  ): FutureOps[T] = new FutureOps[T](future)

  implicit final def infraSyntaxFutureEither[E <: Throwable, T](
    futureEither: Future[Either[E, T]]
  )(implicit ec: ExecutionContext): FutureEitherOps[E, T] =
    new FutureEitherOps[E, T](futureEither)
}

final private[syntax] class FutureOps[T](private val future: Future[T])(implicit
  val ec: ExecutionContext
) {
  def ifFailedThenToInfraError(message: String): Future[T] =
    future.transformWith {
      case Success(v) => Future.successful(v)
      case Failure(e) => Future.failed(DBError(message + "\n" + e.getMessage))
    }
}

final private[syntax] class FutureEitherOps[E <: Throwable, T](
  private val futureEither: Future[Either[E, T]]
)(implicit val ec: ExecutionContext) {
  def ifLeftThenToInfraError(message: String): Future[T] =
    futureEither.transformWith {
      case Success(Right(v)) => Future.successful(v)
      case Success(Left(e))  => Future.failed(APIError(message + e.getMessage))
      case Failure(e)        => Future.failed(APIError(message + e.getMessage))
    }
}
