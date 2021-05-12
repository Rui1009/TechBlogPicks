package helpers.gens

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import org.scalacheck.Gen

object number extends NumberGen

trait NumberGen {
  val longRefinedPositiveGen: Gen[Refined[Long, Positive]] =
    Gen.posNum[Long].map(refineV[Positive].unsafeFrom(_))

  val positiveLongGen: Gen[Long] = Gen.posNum[Long]

  val floatRefinedPositiveGen: Gen[Refined[Float, Positive]] =
    Gen.posNum[Float].map(refineV[Positive].unsafeFrom(_))
}
