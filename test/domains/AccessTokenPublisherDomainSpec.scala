package domains

import domains.accesstokenpublisher.AccessTokenPublisher._
import helpers.traits.ModelSpec
import cats.syntax.either._

class AccessTokenPublisherDomainSpec extends ModelSpec {
  "AccessTokenPublisherToken.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = AccessTokenPublisherToken.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = AccessTokenPublisherToken.create("")
        assert(result.leftSide == EmptyStringError("Token").asLeft)
      }
    }
  }

  "AccessTokenPublisherTemporaryOauthCode.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = AccessTokenPublisherTemporaryOauthCode.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = AccessTokenPublisherTemporaryOauthCode.create("")
        assert(result.leftSide == EmptyStringError("temporaryOauthCode").asLeft)
      }
    }
  }

  "AccessTokenPublisher.publishToken" should {
    "return its token" in {
      forAll(accessTokenPublisherGen) { model =>
        assert(model.publishToken == model.token)
      }
    }
  }
}
