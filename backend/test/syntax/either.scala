package syntax

object either extends EitherSyntax

trait EitherSyntax {

  implicit final def testSyntaxEither[R, L](e: Either[L, R]): EitherOps[R, L] =
    new EitherOps(e)

}

final class EitherOps[R, L](private val e: Either[L, R]) extends AnyVal {

  def unsafeGet: R = e.getOrElse(
    throw new Exception(s"can't get right value from ${e.toString}")
  )

  def unsafeLeftGet: L = e.left
    .getOrElse(throw new Exception(s"can't get left value from ${e.toString}"))
}
