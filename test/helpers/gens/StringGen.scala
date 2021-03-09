package helpers.gens

import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import org.scalacheck.Gen
import org.scalacheck.Gen._

object StringGen {
  val nonEmptyStringGen: Gen[String] =
    Gen.nonEmptyListOf[Char](alphaChar).map(_.mkString)

  val stringRefinedNonEmptyGen: Gen[Refined[String, NonEmpty]] = {
    nonEmptyStringGen.flatMap(s => refineV[NonEmpty](s)).map(_.right.get)
  }

}
