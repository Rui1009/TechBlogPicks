package usecases

import domains.accesstokenpublisher.{AccessTokenPublisher, AccessTokenPublisherRepository}
import domains.bot.BotRepository
import helpers.traits.UseCaseSpec
import usecases.InstallBotUseCase._
import cats.syntax.option._
import infra.DBError

import scala.concurrent.Future

class InstallBotUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val accessTokenRepo = mock[AccessTokenPublisherRepository]
    val botRepo = mock[BotRepository]
    "succeed" should {
      "invoke accessTokenPublisherRepository.find once & botRepository.update once" in {

        forAll(
          temporaryOauthCodeGen,
          botIdGen,
          accessTokenPublisherGen
        ) {(tempOauthCode, botId, accessTokenPublisher) =>
          val params = Params(tempOauthCode, botId)

          when(accessTokenRepo.find(tempOauthCode)).thenReturn(Future.successful(accessTokenPublisher))
          when(botRepo.update(botId, accessTokenPublisher.token)).thenReturn(Future.unit)

          new InstallBotUseCaseImpl(accessTokenRepo, botRepo).exec(params).futureValue

          verify(accessTokenRepo, only).find(tempOauthCode)
          verify(botRepo, only).update(botId, accessTokenPublisher.token)
          reset(accessTokenRepo)
          reset(botRepo)
        }
      }
    }

    "failed in accessTokenPublisherRepository.find" should {
      "throw use case error and not invoked botRepository.update" in {
        forAll(
          temporaryOauthCodeGen,
          botIdGen,
          accessTokenPublisherGen
        ) {(tempOauthCode, botId, accessTokenPublisher) =>
          val params = Params(tempOauthCode, botId)

          when(accessTokenRepo.find(tempOauthCode)).thenReturn(Future.failed(DBError("error")))
          when(botRepo.update(botId, accessTokenPublisher.token)).thenReturn(Future.unit)

          val result = new InstallBotUseCaseImpl(accessTokenRepo, botRepo).exec(params)

          whenReady(result.failed) {e =>
            assert(
              e == SystemError(
                "error while accessTokenPublisher.find in install bot use case"
                  + DBError("error").getMessage
              )
            )
            verify(botRepo, never).update(botId, accessTokenPublisher.token)
          }
        }
      }
    }

    "failed in botRepository.update" should {
      "throw use case error" in {
        forAll(
          temporaryOauthCodeGen,
          botIdGen,
          accessTokenPublisherGen
        ) {(tempOauthCode, botId, accessTokenPublisher) =>
          val params = Params(tempOauthCode, botId)

          when(accessTokenRepo.find(tempOauthCode)).thenReturn(Future.successful(accessTokenPublisher))
          when(botRepo.update(botId, accessTokenPublisher.token)).thenReturn(Future.failed(DBError("error")))

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
