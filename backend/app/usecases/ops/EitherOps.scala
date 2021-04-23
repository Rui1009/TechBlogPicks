package usecases.ops

import scala.concurrent.Future

trait EitherSyntax {
  implicit final def infraSyntaxEither[S <: Throwable, T](
    either: Either[S, T]
  ): EitherOps[S, T] = new EitherOps[S, T](either)
}

final private[ops] class EitherOps[S <: Throwable, T](
  private val either: Either[S, T]
) extends AnyVal {
  def ifLeftThenToUseCaseError: Future[T] = either match {
    case Left(e)  => Future.failed(e)
    case Right(v) => Future.successful(v)
  }
}
