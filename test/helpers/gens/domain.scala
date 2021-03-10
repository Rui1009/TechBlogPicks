package helpers.gens

import domains.accesstokenpublisher.AccessTokenPublisher
import domains.post.Post._
import domains.accesstokenpublisher.AccessTokenPublisher_
import org.scalacheck.Gen
import helpers.gens.string._
import helpers.gens.number._

object domain extends DomainGen

trait DomainGen extends AccessTokenPublisherGen with PostGen

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

trait PostGen {
  val postUrlGen: Gen[PostUrl] = stringRefinedUrlGen.map(PostUrl(_))

  val postTitleGen: Gen[PostTitle] = stringRefinedNonEmptyGen.map(PostTitle(_))

  val postPostedAtGen: Gen[PostPostedAt] =
    longRefinedPositiveGen.map(PostPostedAt(_))
}
