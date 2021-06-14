package domains

import cats.syntax.either._
import domains.post.Post._
import helpers.traits.ModelSpec
import org.scalacheck.Gen

class PostDomainSpec extends ModelSpec {
  "PostId.create" when {
    "given positive long " should {
      "return Right value which equals given arg value" in {
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
        val result = PostId.create(0: Long)
        assert(result.leftSide == NegativeNumberError("PostId").asLeft)
      }
    }
  }

  "PostUrl.create" when {
    "given valid string" should {
      "return Right value which equals given arg value" in {
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
      "return Right value which equals given arg value" in {
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

  "PostAuthor.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = PostAuthor.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = PostAuthor.create("")
        assert(result.leftSide == EmptyStringError("PostAuthor").asLeft)
      }
    }
  }

  "PostTestimonial.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = PostTestimonial.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which equals DomainError" in {
        val result = PostTestimonial.create("")
        assert(result.leftSide == EmptyStringError("PostTestimonial").asLeft)
      }
    }
  }

  "PostedAt.create" when {
    "given positive long " should {
      "return Right value which equals given arg value" in {
        forAll(longRefinedPositiveGen) { n =>
          val result = PostPostedAt.create(n.value)
          assert(result.map(_.value) == n.asRight)
        }
      }
    }

    "given negative long " should {
      "return Left value which equals DomainError" in {
        forAll(Gen.negNum[Long]) { n =>
          val result = PostPostedAt.create(n)
          assert(result.leftSide == NegativeNumberError("PostPostedAt").asLeft)
        }
      }
    }

    "given zero" should {
      "return Left value which value equals DomainError" in {
        val result = PostPostedAt.create(0: Long)
        assert(result.leftSide == NegativeNumberError("PostPostedAt").asLeft)
      }
    }
  }

  "Post.assign" when {
    "given application list" should {
      "return applications which have registered post" in {
        forAll(Gen.nonEmptyListOf(applicationGen), postGen) {
          (_appList, post) =>
            val appList = _appList.map(app =>
              app.copy(posts = app.posts.filter(_ === post.id))
            )

            val result   = post.assign(appList)
            val expected =
              appList.map(app => app.copy(posts = app.posts :+ post.id))

            assert(result === expected)
        }
      }
    }
  }
}
