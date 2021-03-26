package usecases

import domains.bot.BotRepository
import domains.workspace.WorkSpaceRepository
import helpers.traits.UseCaseSpec
import infra.DBError
import usecases.UninstallBotUseCase.Params

import scala.concurrent.Future

class UninstallBotUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val botRepo       = mock[BotRepository]
    val workSpaceRepo = mock[WorkSpaceRepository]

    "succeed" should {
      "invoke botRepository.find once & workSpaceRepository.find once & workSpaceRepository.add once" in {
        forAll(botGen, workSpaceGen) { (bot, workSpace) =>
          val params = Params(bot.id, workSpace.id)

          when(botRepo.find(params.botId)).thenReturn(Future.successful(bot))
          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(workSpace)))
          when(workSpaceRepo.add(workSpace)).thenReturn(Future.unit)

          new UninstallBotUseCaseImpl(botRepo, workSpaceRepo)
            .exec(params)
            .futureValue

          verify(botRepo).find(params.botId)
          verify(workSpaceRepo).find(params.workSpaceId)
          verify(workSpaceRepo).add(workSpace)
          reset(botRepo)
        }
      }
    }

    "failed in botRepository.find" should {
      "throw use case error" in {
        forAll(botGen, workSpaceGen) { (bot, workSpace) =>
          val params = Params(bot.id, workSpace.id)

          when(botRepo.find(params.botId))
            .thenReturn(Future.failed(DBError("error")))

          val result =
            new UninstallBotUseCaseImpl(botRepo, workSpaceRepo).exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === SystemError(
                "error while botRepository.find in uninstall bot use case" + "\n" + DBError(
                  "error"
                ).getMessage
              )
            )
            verify(workSpaceRepo, never).find(params.workSpaceId)
            verify(workSpaceRepo, never).add(workSpace)
          }
        }
      }
    }

    "failed in workSpaceRepository.find" should {
      "return future unit" in {
        forAll(botGen, workSpaceGen) { (bot, workSpace) =>
          val params = Params(bot.id, workSpace.id)

          when(botRepo.find(params.botId)).thenReturn(Future.successful(bot))
          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(None))

          val result: Unit = new UninstallBotUseCaseImpl(botRepo, workSpaceRepo)
            .exec(params)
            .futureValue

          assert(result === ())

        }
      }
    }

    "failed in workSpaceRepository.add" should {
      "throw use case error" in {
        forAll(botGen, workSpaceGen) { (bot, workSpace) =>
          val params = Params(bot.id, workSpace.id)

          when(botRepo.find(params.botId)).thenReturn(Future.successful(bot))
          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(workSpace)))
          when(workSpaceRepo.add(workSpace))
            .thenReturn(Future.failed(DBError("error")))

          val result =
            new UninstallBotUseCaseImpl(botRepo, workSpaceRepo).exec(params)

          whenReady(result.failed) { e =>
            e === SystemError(
              "error while workSpaceRepository.add in uninstall bot use case" + "\n" + DBError(
                "error"
              ).getMessage
            )
          }
        }
      }
    }
  }

}
