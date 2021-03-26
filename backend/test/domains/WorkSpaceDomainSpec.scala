package domains

import domains.workspace.WorkSpace._
import helpers.traits.ModelSpec
import cats.syntax.either._

class WorkSpaceDomainSpec extends ModelSpec {
  "AccessTokenPublisherToken.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = WorkSpaceToken.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = WorkSpaceToken.create("")
        assert(result.leftSide == EmptyStringError("Token").asLeft)
      }
    }
  }

  "AccessTokenPublisherTemporaryOauthCode.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = WorkSpaceTemporaryOauthCode.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = WorkSpaceTemporaryOauthCode.create("")
        assert(result.leftSide == EmptyStringError("temporaryOauthCode").asLeft)
      }
    }
  }

  "AccessTokenPublisher.publishToken" should {
    "return its token" in {
      forAll(accessTokenPublisherGen) { model =>
        assert(model.publishToken == model.tokens)
      }
    }
  }
}
