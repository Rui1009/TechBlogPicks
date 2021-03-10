package helpers.gens

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import org.scalacheck.Gen

object number extends NumberGen

trait NumberGen {
  val longRefinedPositiveGen: Gen[Refined[Long, Positive]] =
    Gen.posNum[Long].map(refineV[Positive].unsafeFrom(_))
}
