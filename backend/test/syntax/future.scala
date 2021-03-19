package syntax

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object future extends FutureSyntax

trait FutureSyntax {
  implicit final def testSyntaxFuture[T](future: Future[T]): FutureOps[T] =
    new FutureOps(future)

}

final class FutureOps[T](private val future: Future[T]) extends AnyVal {
  def ready(): Unit = {
    Await.result(future, Duration.Inf)
    ()
  }

}
