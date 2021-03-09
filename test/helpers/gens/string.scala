package helpers.gens

import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import eu.timepit.refined.string.Url
import org.scalacheck.Gen
import org.scalacheck.Gen._

object string extends StringGen

trait StringGen {
  val nonEmptyStringGen: Gen[String] =
    Gen.nonEmptyListOf[Char](alphaChar).map(_.mkString)

  val stringRefinedNonEmptyGen: Gen[Refined[String, NonEmpty]] =
    nonEmptyStringGen.map(s => refineV[NonEmpty](s).right.get)

  val urlGen: Gen[String] = (for {
    protocol <- oneOf("http", "https")
    domains <- listOfN(3, nonEmptyStringGen)
    dirNum <- choose(1, 5)
    dirs <- listOfN(dirNum, nonEmptyStringGen)
  } yield
    protocol + "://" + domains
      .mkString(".") + dirs.mkString("/", "/", "/")).label("url")

  val stringRefinedUrlGen: Gen[Refined[String, Url]] =
    urlGen.map(refineV[Url].unsafeFrom(_))
}
