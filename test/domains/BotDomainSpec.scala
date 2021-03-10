package domains

import domains.bot.Bot._
import helpers.traits.ModelSpec
import cats.syntax.either._

class BotDomainSpec extends ModelSpec {
  "BotId.create" when {
    "given non-empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = BotId.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = BotId.create("")
        assert(result.leftSide == EmptyStringError("BotId").asLeft)
      }
    }
  }

  "BotName.create" when {
    "given non-empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = BotName.create(str.value)
          assert(result.map(_.value) == str.asRight)
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = BotName.create("")
        assert(result.leftSide == EmptyStringError("BotName").asLeft)
      }
    }
  }

  "Bot.receiveToken" should {
    "return new seq that added token arg" in {
      forAll(botGen, accessTokenGen) { (model, token) =>
        assert(
          model
            .receiveToken(token)
            .accessTokens
            .size == model.accessTokens.size + 1)
        assert(model.receiveToken(token).accessTokens.last == token)
      }
    }
  }
}
