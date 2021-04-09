package domains

import domains.message.Message.{
  AccessoryImage,
  ActionSelect,
  MessageChannelId,
  MessageId,
  MessageSentAt,
  MessageUserId,
  SelectPlaceHolder
}
import helpers.traits.ModelSpec
import cats.syntax.either._
import org.scalacheck.Gen

class MessageDomainSpec extends ModelSpec {
  "MessageId.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = MessageId.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which equals DomainError" in {
        val result = MessageId.create("")
        assert(result.leftSide == EmptyStringError("MessageId").asLeft)
      }
    }
  }

  "MessageSentAt.create" when {
    "given valid string" should {
      "return Right value which equals given arg value" in {
        forAll(refinedValidFloatGen) { str =>
          val result = MessageSentAt.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given invalid string" should {
      "return Left value which equals DomainError" in {
        forAll(refinedValidFloatGen) { str =>
          val result = MessageSentAt.create(s"${str}text")
          assert(result.map(_.value) == RegexError("MessageSentAt").asLeft)
        }
      }
    }
  }

  "MessageUserId.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = MessageUserId.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which equals DomainError" in {
        val result = MessageUserId.create("")
        assert(result.leftSide == EmptyStringError("MessageUserId").asLeft)
      }
    }
  }

  "MessageChannelId.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = MessageChannelId.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which equals DomainError" in {
        val result = MessageChannelId.create("")
        assert(result.leftSide == EmptyStringError("MessageChannelId").asLeft)
      }
    }
  }

  "AccessoryImage.create" when {
    "given imageUrl valid url string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedUrlGen, Gen.alphaStr) { (url, str) =>
          val result = AccessoryImage(url, str)
          assert(result == AccessoryImage(url, str))
        }
      }
    }

    "given iamgeUrl invalid url string" should {
      "return Left value which equals DomainError" in {
        forAll(Gen.alphaStr) { str =>
          val result = AccessoryImage.create(str, str)
          assert(result.leftSide == RegexError("ImageUrl").asLeft)
        }
      }
    }
  }

  "ActionSelect.create" when {
    "given actionType & actionId valid string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = ActionSelect(str, SelectPlaceHolder("", false), str)
          assert(result == ActionSelect(str, SelectPlaceHolder("", false), str))
        }
      }
    }

    "given actionType invalid string" should {
      "return Left value which equals DomainError" in {
        forAll(nonEmptyStringGen) { str =>
          val result =
            ActionSelect.create("", SelectPlaceHolder("", false), str)
          assert(result.leftSide == EmptyStringError("actionType").asLeft)
        }
      }
    }

    "given actionId invalid string" should {
      "return Left value which equals DomainError" in {
        forAll(nonEmptyStringGen) { str =>
          val result =
            ActionSelect.create(str, SelectPlaceHolder("", false), "")
          assert(result.leftSide == EmptyStringError("actionId").asLeft)
        }
      }
    }

    "given actionType & actionId invalid string" should {
      "return Left value which equals DomainError" in {
        val result = ActionSelect.create("", SelectPlaceHolder("", false), "")
        assert(
          result.leftSide == EmptyStringError("actionType, actionId").asLeft
        )
      }
    }
  }
}
