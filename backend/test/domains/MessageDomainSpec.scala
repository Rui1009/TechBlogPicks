package domains

import domains.message.Message.{
  MessageChannelId,
  MessageId,
  MessageSentAt,
  MessageUserId
}
import helpers.traits.ModelSpec
import cats.syntax.either._
import domains.message.Message.AccessoryImage.AccessoryImageUrl
import domains.message.Message.ActionSelect.{SelectActionId, SelectActionType}

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
          val result = MessageSentAt.create(str + "text")
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

  "AccessoryImageUrl.create" when {
    "given valid string url" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedUrlGen) { url =>
          val result = AccessoryImageUrl.create(url.value)
          assert(result.map(_.value) == url.asRight)
        }
      }
    }

    "given invalid string" should {
      "return Left value which equals DomainError" in {
        forAll(nonEmptyStringGen) { str =>
          val result = AccessoryImageUrl.create(str)
          assert(result.leftSide == RegexError("AccessoryImageUrl").asLeft)
        }
      }
    }
  }

  "SelectActionType.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = SelectActionType.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which equals DomainError" in {
        val result = SelectActionType.create("")
        assert(result.leftSide == EmptyStringError("SelectActionType").asLeft)
      }
    }
  }

  "SelectActionId.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = SelectActionId.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which equals DomainError" in {
        val result = SelectActionId.create("")
        assert(result.leftSide == EmptyStringError("SelectActionId").asLeft)
      }
    }
  }
}
