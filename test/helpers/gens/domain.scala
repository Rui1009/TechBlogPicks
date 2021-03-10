package helpers.gens

import domains.accesstokenpublisher.AccessTokenPublisher
import domains.accesstokenpublisher.AccessTokenPublisher.{AccessTokenPublisherTemporaryOauthCode, AccessTokenPublisherToken}
import org.scalacheck.Gen
import helpers.gens.string._

object domain extends DomainGen

trait DomainGen extends AccessTokenPublisherGen

trait AccessTokenPublisherGen {
  val accessTokenGen: Gen[AccessTokenPublisherToken] =
    stringRefinedNonEmptyGen.map(AccessTokenPublisherToken(_))

  val temporaryOauthCodeGen: Gen[AccessTokenPublisherTemporaryOauthCode] =
    stringRefinedNonEmptyGen.map(AccessTokenPublisherTemporaryOauthCode(_))

  val accessTokenPublisherGen: Gen[AccessTokenPublisher] =
    for {
      accessToken <- accessTokenGen
      temporaryOauthCode <- temporaryOauthCodeGen
    } yield AccessTokenPublisher(accessToken, temporaryOauthCode)
}
