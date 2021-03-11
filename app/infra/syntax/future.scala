package infra.syntax

import infra.DBError

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object future extends FutureSyntax

trait FutureSyntax {
  implicit final def infraSyntaxFuture[T](future: Future[T])(implicit
    ec: ExecutionContext
  ): FutureOps[T] = new FutureOps[T](future)
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
