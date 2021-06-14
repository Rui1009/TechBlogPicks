package domains

import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.refineV
import cats.syntax.either._

abstract class VOFactory {
  type Type
  type Repr <: Refined[_, _]

  def apply(v: Repr): Type

  final def apply[V, P](v: V)(implicit
    ev: Refined[V, P] =:= Repr,
    V: Validate[V, P]
  ): Either[SomeDomainError, Type] = refineV[P](v).bimap(
    e => SomeDomainError(s"Failed to create ${this.toString}. Cause: $e"),
    a => apply(ev(a))
  )

  final def unsafeFrom[V, P](v: V)(implicit ev: Refined[V, P] =:= Repr): Type =
    apply(Refined.unsafeApply[V, P](v))
}
