package domains

import domains.bot.Bot._
import helpers.traits.ModelSpec
import org.scalacheck.Gen

class BotDomainSpec extends ModelSpec {
  "BotId.create" when {
    "given non-empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = BotId.create(str.value)
          assert(result.map(_.value) === Right(str))
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = BotId.create("")
        assert(result.leftSide === Left(EmptyStringError("BotId")))
      }
    }
  }

  "BotName.create" when {
    "given non-empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = BotName.create(str.value)
          assert(result.map(_.value) === Right(str))
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = BotName.create("")
        assert(result.leftSide === Left(EmptyStringError("BotName")))
      }
    }
  }

  "BotAccessToken.create" when {
    "given non-empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = BotAccessToken.create(str.value)
          assert(result.map(_.value) === Right(str))
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = BotAccessToken.create("")
        assert(result.leftSide === Left(EmptyStringError("BotAccessToken")))
      }
    }
  }

  "Bot.joinTo" should {
    "return Bot which channelIds is updated" in {
      forAll(botGen, channelIdGen) { (bot, channelId) =>
        val result = bot.joinTo(channelId)
        assert(result.channelIds.contains(channelId))
        assert(result.channelIds.length === bot.channelIds.length + 1)
      }
    }
  }

  "Bot.postMessage" should {
    "return channel which messages is updated" in {
//      forAll(botGen, channelGen, messageGen) { (bot, channel, message) => }
      pending
    }
  }
//  "BotClientId.create" when {
//    "given non-empty string" should {
//      "return Right value which equals given arg value" in {
//        forAll(stringRefinedNonEmptyGen) { str =>
//          val result = BotClientId.create(str.value)
//          assert(result.map(_.value) === Right(str))
//        }
//      }
//    }
//
//    "given empty string" should {
//      "return Left value which values equals DomainError" in {
//        val result = BotClientId.create("")
//        assert(result.leftSide === Left(EmptyStringError("BotClientId")))
//      }
//    }
//  }
//
//  "BotClientSecret.create" when {
//    "given non-empty string" should {
//      "return Right value which equals given arg value" in {
//        forAll(stringRefinedNonEmptyGen) { str =>
//          val result = BotClientSecret.create(str.value)
//          assert(result.map(_.value) === Right(str))
//        }
//      }
//    }
//
//    "given empty string" should {
//      "return Left value which values equals DomainError" in {
//        val result = BotClientSecret.create("")
//        assert(result.leftSide === Left(EmptyStringError("BotClientSecret")))
//      }
//    }
//  }
//
//  "BotChannelId.create" when {
//    "given non-empty string" should {
//      "return Right value which equals given arg value" in {
//        forAll(stringRefinedNonEmptyGen) { str =>
//          val result = BotChannelId.create(str.value)
//          assert(result.map(_.value) === Right(str))
//        }
//      }
//    }
//
//    "given empty string" should {
//      "return Left value which values equals DomainError" in {
//        val result = BotChannelId.create("")
//        assert(result.leftSide === Left(EmptyStringError("BotChannelId")))
//      }
//    }
//  }
//
//  "Bot.updateClientInfo" should {
//    "return Bot model which client info updated" in {
//      forAll(
//        botGen,
//        Gen.option(botClientIdGen),
//        Gen.option(botClientSecretGen)
//      ) { (model, id, secret) =>
//        val result = model.updateClientInfo(id, secret)
//
//        assert(result.clientId === id)
//        assert(result.clientSecret === secret)
//      }
//    }
//  }
}
