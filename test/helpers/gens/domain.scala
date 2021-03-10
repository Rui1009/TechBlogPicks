package helpers.gens

import domains.accesstokenpublisher.AccessTokenPublisher
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import domains.post.Post._
import org.scalacheck.Gen
import helpers.gens.string._
import helpers.gens.number._

object domain extends DomainGen

trait DomainGen extends AccessTokenPublisherGen with PostGen

trait AccessTokenPublisherGen {
  val accessTokenGen: Gen[AccessTokenPublisherToken] =
    stringRefinedNonEmptyGen.map(AccessTokenPublisherToken(_))

  val accessTokenPublisherGen: Gen[AccessTokenPublisher] =
    accessTokenGen.map(AccessTokenPublisher(_))
}

trait PostGen {
  val postUrlGen: Gen[PostUrl] = stringRefinedUrlGen.map(PostUrl(_))

  val postTitleGen: Gen[PostTitle] = stringRefinedNonEmptyGen.map(PostTitle(_))

  val postPostedAtGen: Gen[PostPostedAt] =
    longRefinedPositiveGen.map(PostPostedAt(_))
}
