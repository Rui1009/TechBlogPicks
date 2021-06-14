package domains

import cats.syntax.either._
import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.refineV

abstract class VOFactory[Error <: DomainError] {
  type Type
  type Repr <: Refined[_, _]

  def castError(e: ValidationError): Error

  def apply(v: Repr): Type

  final def apply[V, P](v: V)(implicit
    ev: Refined[V, P] =:= Repr,
    V: Validate[V, P]
  ): Either[ValidationError, Type] = refineV[P](v).bimap(
    e => ValidationError(s"Failed to create ${this.toString}. Cause: $e"),
    a => apply(ev(a))
  )

  final def create[V, P](v: V)(implicit
    ev: Refined[V, P] =:= Repr,
    V: Validate[V, P]
  ): Either[Error, Type] = apply(v).leftMap(castError)

  final def unsafeFrom[V, P](v: V)(implicit ev: Refined[V, P] =:= Repr): Type =
    apply(Refined.unsafeApply[V, P](v))
}
