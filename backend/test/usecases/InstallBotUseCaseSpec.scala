package usecases

import domains.accesstokenpublisher.AccessTokenPublisherRepository
import domains.bot.Bot.{BotClientId, BotClientSecret}
import domains.bot.{Bot, BotRepository}
import helpers.traits.UseCaseSpec
import usecases.InstallBotUseCase._
import infra.DBError
import eu.timepit.refined.auto._

import scala.concurrent.Future

class InstallBotUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val accessTokenRepo = mock[AccessTokenPublisherRepository]
    val botRepo         = mock[BotRepository]
    "succeed" should {
      "invoke botRepository.find once & accessTokenPublisherRepository.find once & botRepository.update once" in {
        forAll(
          temporaryOauthCodeGen,
          nonOptionBotGen,
          accessTokenPublisherGen
        ) { (tempOauthCode, bot, accessTokenPublisher) =>
          val params = Params(tempOauthCode, bot.id)

          when(botRepo.find(params.botId)).thenReturn(Future.successful(bot))
          when(
            accessTokenRepo.find(
              params.temporaryOauthCode,
              bot.clientId.get,
              bot.clientSecret.get
            )
          ).thenReturn(Future.successful(Some(accessTokenPublisher)))
          when(
            botRepo.update(
              bot.receiveToken(accessTokenPublisher.publishToken),
              accessTokenPublisher.publishToken
            )
          ).thenReturn(Future.unit)

          new InstallBotUseCaseImpl(accessTokenRepo, botRepo)
            .exec(params)
            .futureValue

          verify(botRepo).find(params.botId)
          verify(accessTokenRepo, only).find(
            params.temporaryOauthCode,
            bot.clientId.get,
            bot.clientSecret.get
          )
          verify(botRepo).update(
            bot.receiveToken(accessTokenPublisher.publishToken),
            accessTokenPublisher.publishToken
          )
          reset(accessTokenRepo)
          reset(botRepo)
        }
      }
    }

    "failed in botRepository.find" should {
      "throw use case error and not invoked accessTokenPublisherRepository.find & botRepository.update" in {
        forAll(
          temporaryOauthCodeGen,
          nonOptionBotGen,
          accessTokenPublisherGen
        ) { (tempOauthCode, bot, accessTokenPublisher) =>
          val params = Params(tempOauthCode, bot.id)

          when(botRepo.find(params.botId))
            .thenReturn(Future.failed(DBError("error")))
          when(
            accessTokenRepo.find(
              params.temporaryOauthCode,
              bot.clientId.get,
              bot.clientSecret.get
            )
          ).thenReturn(Future.successful(Some(accessTokenPublisher)))
          when(
            botRepo.update(
              bot.receiveToken(accessTokenPublisher.publishToken),
              accessTokenPublisher.publishToken
            )
          ).thenReturn(Future.unit)

          val result =
            new InstallBotUseCaseImpl(accessTokenRepo, botRepo).exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === SystemError(
                "error while botRepository.find in install bot use case" + "\n"
                  + DBError("error").getMessage
              )
            )
            verify(accessTokenRepo, never).find(
              params.temporaryOauthCode,
              bot.clientId.get,
              bot.clientSecret.get
            )
            verify(botRepo, never).update(
              bot.receiveToken(accessTokenPublisher.publishToken),
              accessTokenPublisher.publishToken
            )
          }
        }
      }
    }

    "returned clientId which is None" should {
      "throw use case error and not invoked accessTokenPublisherRepository.find and botRepository.update" in {
        forAll(temporaryOauthCodeGen, botGen, accessTokenPublisherGen) {
          (tempOauthCode, bot, accessTokenPublisher) =>
            val params = Params(tempOauthCode, bot.id)

            when(botRepo.find(params.botId)).thenReturn(
              Future.successful(
                bot.updateClientInfo(None, Some(BotClientSecret("test")))
              )
            )

            val result =
              new InstallBotUseCaseImpl(accessTokenRepo, botRepo).exec(params)

            whenReady(result.failed) { e =>
              assert(
                e === SystemError(
                  "error while get bot client id in install bot use case"
                )
              )
            }
        }
      }
    }

    "returned clientSecret which is None" should {
      "throw use case error and not invoked accessTokenPublisherRepository.find and botRepository.update" in {
        forAll(temporaryOauthCodeGen, botGen, accessTokenPublisherGen) {
          (tempOauthCode, bot, accessTokenPublisher) =>
            val params = Params(tempOauthCode, bot.id)

            when(botRepo.find(params.botId)).thenReturn(
              Future.successful(
                bot.updateClientInfo(Some(BotClientId("test")), None)
              )
            )

            val result =
              new InstallBotUseCaseImpl(accessTokenRepo, botRepo).exec(params)

            whenReady(result.failed) { e =>
              assert(
                e === SystemError(
                  "error while get bot client secret in install bot use case"
                )
              )
            }
        }
      }
    }

    "failed in accessTokenPublisherRepository.find" should {
      "throw use case error and not invoked botRepository.update" in {
        forAll(
          temporaryOauthCodeGen,
          nonOptionBotGen,
          accessTokenPublisherGen
        ) { (tempOauthCode, bot, accessTokenPublisher) =>
          val params = Params(tempOauthCode, bot.id)

          when(botRepo.find(params.botId)).thenReturn(Future.successful(bot))
          when(
            accessTokenRepo.find(
              params.temporaryOauthCode,
              bot.clientId.get,
              bot.clientSecret.get
            )
          ).thenReturn(Future.successful(None))
          when(
            botRepo.update(
              bot.receiveToken(accessTokenPublisher.publishToken),
              accessTokenPublisher.publishToken
            )
          ).thenReturn(Future.unit)

          val result =
            new InstallBotUseCaseImpl(accessTokenRepo, botRepo).exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === NotFoundError(
                "error while accessTokenPublisherRepository.find in install bot use case"
              )
            )
            verify(botRepo, never).update(
              bot.receiveToken(accessTokenPublisher.publishToken),
              accessTokenPublisher.publishToken
            )
          }
        }
      }
    }

    "failed in botRepository.update" should {
      "throw use case error" in {
        forAll(
          temporaryOauthCodeGen,
          nonOptionBotGen,
          accessTokenPublisherGen
        ) { (tempOauthCode, bot, accessTokenPublisher) =>
          val params = Params(tempOauthCode, bot.id)

          when(botRepo.find(params.botId)).thenReturn(Future.successful(bot))
          when(
            accessTokenRepo.find(
              params.temporaryOauthCode,
              bot.clientId.get,
              bot.clientSecret.get
            )
          ).thenReturn(Future.successful(Some(accessTokenPublisher)))
          when(
            botRepo.update(
              bot.receiveToken(accessTokenPublisher.publishToken),
              accessTokenPublisher.publishToken
            )
          ).thenReturn(Future.failed(DBError("error")))

          val result =
            new InstallBotUseCaseImpl(accessTokenRepo, botRepo).exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === SystemError(
                "error while botRepository.update in install bot use case" + "\n"
                  + DBError("error").getMessage
              )
            )
          }
        }
      }
    }
  }
}
