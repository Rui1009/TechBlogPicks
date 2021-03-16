package usecases

import domains.accesstokenpublisher.{AccessTokenPublisherRepository}
import domains.bot.BotRepository
import helpers.traits.UseCaseSpec
import usecases.InstallBotUseCase._
import infra.DBError

import scala.concurrent.Future

class InstallBotUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val accessTokenRepo = mock[AccessTokenPublisherRepository]
    val botRepo = mock[BotRepository]
    "succeed" should {
      "invoke botRepository.find once & accessTokenPublisherRepository.find once & botRepository.update once" in {
        forAll(
          temporaryOauthCodeGen,
          botGen,
          accessTokenPublisherGen
        ) {(tempOauthCode, bot, accessTokenPublisher) =>
          val params = Params(tempOauthCode, bot.id)

          when(botRepo.find(params.botId)).thenReturn(Future.successful(bot))
          when(accessTokenRepo.find(params.temporaryOauthCode)).thenReturn(Future.successful(Some(accessTokenPublisher)))
          when(botRepo.update(bot.receiveToken(accessTokenPublisher.publishToken))).thenReturn(Future.unit)

          new InstallBotUseCaseImpl(accessTokenRepo, botRepo).exec(params).futureValue

          verify(botRepo).find(params.botId)
          verify(accessTokenRepo, only).find(params.temporaryOauthCode)
          verify(botRepo).update(bot.receiveToken(accessTokenPublisher.publishToken))
          reset(accessTokenRepo)
          reset(botRepo)
        }
      }
    }

    "failed in botRepository.find" should {
      "throw use case error and not invoked accessTokenPublisherRepository.find & botRepository.update" in {
        forAll(
          temporaryOauthCodeGen,
          botGen,
          accessTokenPublisherGen
        ) {(tempOauthCode, bot, accessTokenPublisher) =>
          val params = Params(tempOauthCode, bot.id)

          when(botRepo.find(params.botId)).thenReturn(Future.failed(DBError("error")))
          when(accessTokenRepo.find(params.temporaryOauthCode)).thenReturn(Future.successful(Some(accessTokenPublisher)))
          when(botRepo.update(bot.receiveToken(accessTokenPublisher.publishToken))).thenReturn(Future.unit)

          val result = new InstallBotUseCaseImpl(accessTokenRepo, botRepo).exec(params)

          whenReady(result.failed) {e =>
            assert(
              e == SystemError(
                "error while botRepository.find in install bot use case"
                  + DBError("error").getMessage
              )
            )
            verify(accessTokenRepo, never).find(params.temporaryOauthCode)
            verify(botRepo, never).update(bot.receiveToken(accessTokenPublisher.publishToken))
          }
        }
      }
    }

    "failed in accessTokenPublisherRepository.find" should {
      "throw use case error and not invoked botRepository.update" in {
        forAll(
          temporaryOauthCodeGen,
          botGen,
          accessTokenPublisherGen
        ) {(tempOauthCode, bot, accessTokenPublisher) =>
          val params = Params(tempOauthCode, bot.id)

          when(botRepo.find(params.botId)).thenReturn(Future.successful(bot))
          when(accessTokenRepo.find(params.temporaryOauthCode)).thenReturn(Future.successful(None))
          when(botRepo.update(bot.receiveToken(accessTokenPublisher.publishToken))).thenReturn(Future.unit)

          val result = new InstallBotUseCaseImpl(accessTokenRepo, botRepo).exec(params)

          whenReady(result.failed) {e =>
            assert(
              e == NotFoundError("error while accessTokenPublisherRepository.find in install bot use case")
            )
            verify(botRepo, never).update(bot.receiveToken(accessTokenPublisher.publishToken))
          }
        }
      }
    }

    "failed in botRepository.update" should {
      "throw use case error" in {
        forAll(
          temporaryOauthCodeGen,
          botGen,
          accessTokenPublisherGen
        ) {(tempOauthCode, bot, accessTokenPublisher) =>
          val params = Params(tempOauthCode, bot.id)

          when(botRepo.find(params.botId)).thenReturn(Future.successful(bot))
          when(accessTokenRepo.find(params.temporaryOauthCode)).thenReturn(Future.successful(Some(accessTokenPublisher)))
          when(botRepo.update(bot.receiveToken(accessTokenPublisher.publishToken))).thenReturn(Future.failed(DBError("error")))

          val result = new InstallBotUseCaseImpl(accessTokenRepo, botRepo).exec(params)

          whenReady(result.failed) { e =>
            assert(
              e == SystemError(
                "error while botRepository.update in install bot use case"
                  + DBError("error").getMessage
              )
            )
          }
        }
      }
    }
  }
}
