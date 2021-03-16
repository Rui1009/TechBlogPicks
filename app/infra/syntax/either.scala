package infra.syntax

import scala.concurrent.Future

object either extends InfraSyntax

trait InfraSyntax {
  implicit final def infraSyntaxEither[S, T](
    either: Either[S, T]
  ): EitherOps[S, T] = new EitherOps[S, T](either)
}

final private[syntax] class EitherOps[S, T](private val either: Either[S, T]) extends AnyVal {
  def ifLeftThenReturnNone: Option[T] = either match {
    case Left(_)  => None
    case Right(v) => Some(v)
  }
}
