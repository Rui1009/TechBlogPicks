package helpers.gens

import domains.accesstokenpublisher.AccessTokenPublisher
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import org.scalacheck.Gen
import helpers.gens.StringGen.stringRefinedNonEmptyGen

object AccessTokenPublisherGen {
  val accessTokenGen: Gen[AccessTokenPublisherToken] =
    stringRefinedNonEmptyGen.map(AccessTokenPublisherToken(_))

  val accessTokenPublisherGen: Gen[AccessTokenPublisher] =
    accessTokenGen.map(AccessTokenPublisher(_))
}
