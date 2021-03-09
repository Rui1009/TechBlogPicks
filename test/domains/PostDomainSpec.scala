package domains

import helpers.traits.ModelSpec
import cats.syntax.either._
import domains.post.Post._
import org.scalacheck.Gen

class PostDomainSpec extends ModelSpec {
  "PostId.create" when {
    "given positive long " should {
      "return Right value witch equals given arg value" in {
        forAll(longRefinedPositiveGen) { n =>
          val result = PostId.create(n.value)
          assert(result.map(_.value) == n.asRight)
        }
      }
    }

    "given negative long " should {
      "return Left value which equals DomainError" in {
        forAll(Gen.negNum[Long]) { n =>
          val result = PostId.create(n)
          assert(result.leftSide == NegativeNumberError("PostId").asLeft)
        }
      }
    }

    "given zero" should {
      "return Left value which value equals DomainError" in {
        val result = PostId.create(0)
        assert(result.leftSide == NegativeNumberError("PostId").asLeft)
      }
    }
  }

  "PostUrl.create" when {
    "given valid string" should {
      "return Right value witch equals given arg value" in {
        forAll(stringRefinedUrlGen) { url =>
          val result = PostUrl.create(url.value)
          assert(result.map(_.value) == url.asRight)
        }
      }
    }

    "given invalid string" should {
      "return Left value which values equals DomainError" in {
        forAll { str: String =>
          val result = PostUrl.create(str)
          assert(result.leftSide == RegexError("PostUrl").asLeft)
        }
      }
    }
  }

  "PostTitle.create" when {
    "given non empty string" should {
      "return Right value witch equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = PostTitle.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = PostTitle.create("")
        assert(result.leftSide == EmptyStringError("PostTitle").asLeft)
      }
    }
  }

  "PostedAt.create" when {
    "given positive long " should {
      "return Right value witch equals given arg value" in {
        forAll(longRefinedPositiveGen) { n =>
          val result = PostedAt.create(n.value)
          assert(result.map(_.value) == n.asRight)
        }
      }
    }

    "given negative long " should {
      "return Left value which equals DomainError" in {
        forAll(Gen.negNum[Long]) { n =>
          val result = PostedAt.create(n)
          assert(result.leftSide == NegativeNumberError("PostedAt").asLeft)
        }
      }
    }

    "given zero" should {
      "return Left value which value equals DomainError" in {
        val result = PostedAt.create(0)
        assert(result.leftSide == NegativeNumberError("PostedAt").asLeft)
      }
    }
  }
}
