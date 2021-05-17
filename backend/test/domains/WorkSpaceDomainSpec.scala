package domains

import domains.workspace.WorkSpace._
import helpers.traits.ModelSpec
import domains.bot.Bot
import domains.bot.Bot.BotName
import org.scalacheck.Gen

class WorkSpaceDomainSpec extends ModelSpec {
  "WorkSpaceId.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = WorkSpaceId.create(str.value)
          assert(result.map(_.value) === Right(str))
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = WorkSpaceId.create("")
        assert(result.leftSide === Left(EmptyStringError("WorkSpaceId")))
      }
    }
  }

  "WorkSpaceTemporaryOauthCode.create" when {
    "given non empty string" should {
      "return Right value which equals given arg value" in {
        forAll(stringRefinedNonEmptyGen) { str =>
          val result = WorkSpaceTemporaryOauthCode.create(str.value)
          assert(result.map(_.value) === Right(str))
        }
      }
    }

    "given empty string" should {
      "return Left value which values equals DomainError" in {
        val result = WorkSpaceTemporaryOauthCode.create("")
        assert(result === Left(EmptyStringError("WorkSpaceTemporaryOauthCode")))
      }
    }
  }

  "WorkSpace.installApplication" when {
    "Application has some token" should {
      "return WorkSpace which bots are updated" in {
        forAll(workSpaceGen, applicationGen, accessTokensGen) {
          (_workSpace, app, token) =>
            val workSpace = _workSpace.copy(unallocatedToken = Some(token))
            val result    = workSpace.installApplication(app)
            val bot       =
              Bot(None, BotName(app.name.value), app.id, token, Seq(), None)
            val expected  = Right(workSpace.copy(bots = workSpace.bots :+ bot))

            assert(result === expected)
        }
      }
    }

    "Application has none token" should {
      "return domain error" in {
        forAll(workSpaceGen, applicationGen) { (_workSpace, app) =>
          val workSpace = _workSpace.copy(unallocatedToken = None)
          val result    = workSpace.installApplication(app)

          assert(result === Left(NotExistError("unallocatedToken")))
        }
      }
    }
  }

  "WorkSpace.uninstallApplication" should {
    "return WorkSpace which bots are updated" in {
      forAll(workSpaceGen, applicationGen, botGen) { (_workSpace, app, bot) =>
        val installedBot = bot.copy(applicationId = app.id)
        val workSpace    = _workSpace.copy(bots = _workSpace.bots :+ installedBot)
        val result       = workSpace.uninstallApplication(app)
        val expected     = workSpace.copy(bots =
          workSpace.bots.filter(_.applicationId != app.id)
        )

        assert(result === expected)
      }
    }
  }

  "WorkSpace.isChannelExists" when {
    "channel exists" should {
      "return true" in {
        forAll(workSpaceGen, channelTypedChannelMessageGen) {
          (_workSpace, channel) =>
            val workSpace =
              _workSpace.copy(channels = _workSpace.channels :+ channel)
            val result    = workSpace.isChannelExists(channel.id)

            assert(result)
        }
      }
    }

    "channel doesn't exist" should {
      "return false" in {
        forAll(workSpaceGen, channelIdGen) { (_workSpace, id) =>
          val workSpace =
            _workSpace.copy(channels = _workSpace.channels.filter(_.id !== id))
          val result    = workSpace.isChannelExists(id)

          assert(!result)
        }
      }
    }
  }

  "WorkSpace.addBot" should {
    "return workSpace which bot is updated" in {
      forAll(workSpaceGen, botGen) { (workSpace, bot) =>
        val result   = workSpace.addBot(bot)
        val expected = workSpace.copy(bots = workSpace.bots :+ bot)

        assert(result === expected)
      }
    }
  }

  "WorkSpace.addBotToChannel" when {
    "given right args" should {
      "return WorkSpace which bots is updated" in {
        forAll(workSpaceGen, channelTypedChannelMessageGen, botGen) {
          (_workSpace, channel, _bot) =>
            val channels  =
              _workSpace.channels.filter(_.id !== channel.id) :+ channel
            val bot       = _bot.copy(channelIds =
              _bot.channelIds.filter(id => id !== channel.id)
            )
            val bots      = _workSpace.bots.filter(_.id !== bot.id) :+ bot
            val workSpace = _workSpace.copy(channels = channels, bots = bots)

            val result =
              workSpace.addBotToChannel(bot.applicationId, channel.id)

            val updatedBot = bot.copy(channelIds = bot.channelIds :+ channel.id)
            val expected   = Right(
              workSpace.copy(bots =
                workSpace.bots.filter(_.id != bot.id) :+ updatedBot
              )
            )

            assert(result === expected)
        }
      }
    }

    "given not exist application id" should {
      "return domain error which message is right" in {
        forAll(workSpaceGen, channelTypedChannelMessageGen, botGen) {
          (_workSpace, channel, bot) =>
            val workSpace = _workSpace.copy(
              bots =
                _workSpace.bots.filter(_.applicationId !== bot.applicationId),
              channels = _workSpace.channels :+ channel
            )

            val result =
              workSpace.addBotToChannel(bot.applicationId, channel.id)

            assert(
              result.unsafeLeftGet.errorMessage.trim === "NotExistError: ApplicationId don't exist"
            )
        }
      }
    }
  }

  "WorkSpace.findChannel" when {
    "given exist channel id" should {
      "return channel" in {
        forAll(workSpaceGen, channelTypedChannelMessageGen) {
          (_workSpace, channel) =>
            val workSpace = _workSpace.copy(channels =
              _workSpace.channels.filter(_.id !== channel.id) :+ channel
            )

            val result   = workSpace.findChannel(channel.id)
            val expected = Right(channel)

            assert(result === expected)
        }
      }
    }

    "given not exist channel id" should {
      "return domain error" in {
        forAll(workSpaceGen, channelIdGen) { (_workSpace, channelId) =>
          val workSpace = _workSpace.copy(channels =
            _workSpace.channels.filter(_.id !== channelId)
          )

          val result   = workSpace.findChannel(channelId)
          val expected = Left(NotExistError("ChannelId"))

          assert(result === expected)
        }
      }
    }
  }

  "WorkSpace.botCreateOnboardingMessage" when {
    "given exist bot id" should {
      "return draft message" in {
        forAll(workSpaceGen, botGen, applicationIdGen) {
          (_workSpace, _bot, appId) =>
            val bot       = _bot.copy(applicationId = appId)
            val workSpace = _workSpace.copy(bots =
              _workSpace.bots.filter(_.id !== bot.id) :+ bot
            )

            val result   = workSpace.botCreateOnboardingMessage(appId)
            val expected = Right(
              workSpace.copy(bots =
                workSpace.bots
                  .filter(b => b.id != bot.id) :+ bot.createOnboardingMessage
              )
            )

            assert(result === expected)
        }
      }
    }

    "given not exist bot id" should {
      "return domain error" in {
        forAll(workSpaceGen, botGen, applicationIdGen) {
          (_workSpace, _bot, appId) =>
            val bot       = _bot.copy(applicationId = appId)
            val workSpace =
              _workSpace.copy(bots = _workSpace.bots.filter(_.id !== bot.id))

            val result   = workSpace.botCreateOnboardingMessage(appId)
            val expected = Left(NotExistError("Bot"))

            assert(result === expected)
        }
      }
    }
  }

  "WorkSpace.botPostMessage" when {
    "given right args" should {
      "return work space" in {
        forAll(workSpaceGen, botGen, channelTypedChannelMessageGen) {
          (_workSpace, _bot, channel) =>
            val bot       = _bot.createOnboardingMessage
            val workSpace = _workSpace.copy(
              bots = _workSpace.bots
                .filter(_.applicationId !== bot.applicationId) :+ bot,
              channels =
                _workSpace.channels.filter(_.id !== channel.id) :+ channel
            )

            val updatedChannel = bot.postMessage(channel).unsafeGet

            val result   = workSpace.botPostMessage(bot.applicationId, channel.id)
            val expected = Right(
              workSpace.copy(channels =
                workSpace.channels.filter(_.id !== channel.id) :+ updatedChannel
              )
            )

            assert(result === expected)
        }
      }
    }

    "given not exist bot application id" should {
      "return Domain error" in {
        forAll(workSpaceGen, applicationIdGen, channelIdGen) {
          (_workSpace, appId, channelId) =>
            val workSpace = _workSpace.copy(bots =
              _workSpace.bots.filter(_.applicationId !== appId)
            )

            val result   = workSpace.botPostMessage(appId, channelId)
            val expected = Left(NotExistError("Bot"))

            assert(result === expected)
        }
      }
    }

    "given not exist channel id" should {
      "return Domain error" in {
        forAll(workSpaceGen, botGen, channelIdGen) {
          (_workSpace, bot, channelId) =>
            val workSpace = _workSpace.copy(
              bots = _workSpace.bots
                .filter(_.applicationId !== bot.applicationId) :+ bot,
              channels = _workSpace.channels.filter(_.id !== channelId)
            )

            val result   = workSpace.botPostMessage(bot.applicationId, channelId)
            val expected = Left(NotExistError("ChannelId"))

            assert(result === expected)
        }
      }
    }

    "given not exist bot draft message" should {
      "return Domain error" in {
        forAll(workSpaceGen, botGen, channelTypedChannelMessageGen) {
          (_workSpace, bot, channel) =>
            val workSpace = _workSpace.copy(
              bots = _workSpace.bots.filter(
                _.applicationId !== bot.applicationId
              ) :+ bot.copy(draftMessage = None),
              channels =
                _workSpace.channels.filter(_.id !== channel.id) :+ channel
            )

            val result   = workSpace.botPostMessage(bot.applicationId, channel.id)
            val expected = Left(NotExistError("DraftMessage"))

            assert(result === expected)
        }
      }
    }
  }
}
