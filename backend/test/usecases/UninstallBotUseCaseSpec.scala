package usecases

import domains.bot.BotRepository
import helpers.traits.UseCaseSpec
import infra.DBError
import usecases.UninstallBotUseCase.Params

import scala.concurrent.Future

class UninstallBotUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val botRepo = mock[BotRepository]
    "succeed" should {
      "invoke botRepository.update once" in {
        forAll(accessTokenGen) { token =>
          val params = Params(token)

          when(botRepo.update(token)).thenReturn(Future.unit)

          new UninstallBotUseCaseImpl(botRepo).exec(params).futureValue

          verify(botRepo).update(params.accessToken)
          reset(botRepo)
        }
      }
    }

    "failed in botRepository.update" should {
      "throw use case error" in {
        forAll(accessTokenGen) { token =>
          val params = Params(token)
          when(botRepo.update(params.accessToken))
            .thenReturn(Future.failed(DBError("error")))

          val result = new UninstallBotUseCaseImpl(botRepo).exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === SystemError(
                "error while botRepository.update in uninstall bot use case" + "\n" + DBError(
                  "error"
                ).getMessage
              )
            )
          }
        }
      }
    }

  }

}
