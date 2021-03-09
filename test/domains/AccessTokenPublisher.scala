package domains

import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import helpers.gens.StringGen._
import helpers.gens.AccessTokenPublisherGen._
import helpers.traits.ModelSpec

class AccessTokenPublisher extends ModelSpec {
  "AccessTokenPublisherToken.create" when {
    "given nonEmptyString" should {
      "return Either, " which {
        "type is Right" in {
          forAll(nonEmptyStringGen) { str =>
            val result = AccessTokenPublisherToken.create(str)
            assert(result.isRight)
          }
        }
        "value equals given arg string" in {
          forAll(stringRefinedNonEmptyGen) { str =>
            val result = AccessTokenPublisherToken.create(str.value)
            assert(result.right.value === str)
          }
        }
      }
    }
  }

  "AccessTokenPublisher.publishToken" should {
    "return its token" in {
      forAll(accessTokenPublisherGen) { model =>
        assert(model.publishToken === model.token)
      }
    }
  }
}
