package helpers.gens

import domains.accesstokenpublisher.AccessTokenPublisher
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import org.scalacheck.Gen
import helpers.gens.string._

object domain extends DomainGen

trait DomainGen extends AccessTokenPublisherGen

trait AccessTokenPublisherGen {
  val accessTokenGen: Gen[AccessTokenPublisherToken] =
    stringRefinedNonEmptyGen.map(AccessTokenPublisherToken(_))

  val accessTokenPublisherGen: Gen[AccessTokenPublisher] =
    accessTokenGen.map(AccessTokenPublisher(_))
}
