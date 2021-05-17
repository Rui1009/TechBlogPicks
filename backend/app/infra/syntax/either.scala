package infra.syntax

import infra.APIError

import scala.concurrent.Future

object either extends InfraSyntax

trait InfraSyntax {
  implicit final def infraSyntaxEither[S <: Throwable, T](
    either: Either[S, T]
  ): EitherOps[S, T] = new EitherOps[S, T](either)
}

final private[syntax] class EitherOps[S <: Throwable, T](
  private val either: Either[S, T]
) extends AnyVal {
  def ifLeftThenReturnNone: Option[T] = either match {
    case Left(_)  => None
    case Right(v) => Some(v)
  }

  def ifLeftThenToInfraError(message: String): Future[T] = either match {
    case Left(e)  => Future.failed(APIError(message + "\n" + e.getMessage))
    case Right(v) => Future.successful(v)
  }
}
