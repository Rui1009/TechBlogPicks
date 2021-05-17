package domains

import domains.bot.Bot._
import domains.channel.DraftMessage
import domains.channel.DraftMessage.{
  ActionBlock,
  ActionSelect,
  BlockText,
  SectionBlock,
  SelectPlaceHolder
}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import helpers.traits.ModelSpec

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

  "Bot.postMessage" when {
    "bot has draft message" should {
      "return channel which messages is updated" in {
        forAll(botGen, channelTypedChannelMessageGen) { (_bot, channel) =>
          val bot = _bot.createOnboardingMessage

          val result   = bot.postMessage(channel)
          val expected = Right(
            channel.copy(history = channel.history :+ bot.draftMessage.get)
          )

          assert(result === expected)
        }
      }
    }

    "bot does not have draft message" should {
      "return domain error" in {
        forAll(botGen, channelTypedChannelMessageGen) { (bot, channel) =>
          val result   = bot.postMessage(channel)
          val expected = Left(NotExistError("DraftMessage"))

          assert(result === expected)
        }
      }
    }
  }

  "Bot.createOnboardingMessage" should {
    "return bot which draft message is Defined" in {
      forAll(botGen) { bot =>
        val result = bot.createOnboardingMessage
        val draft  = DraftMessage(
          Seq(
            SectionBlock(
              BlockText(
                Refined.unsafeApply(
                  "インストールありがとうございます🤗\nWinkieはあなたの関心のある分野に関する最新の技術記事を自動でslack上に定期配信するアプリです。\nご利用いただくために、初めにアプリを追加するチャンネルを選択してください。"
                )
              ),
              None
            ),
            ActionBlock(
              Seq(
                ActionSelect(
                  "channels_select",
                  SelectPlaceHolder("Select a channel", false),
                  "actionId-0"
                )
              )
            )
          )
        )

        val expected = bot.copy(draftMessage = Some(draft))

        assert(result === expected)
      }
    }
  }
}
