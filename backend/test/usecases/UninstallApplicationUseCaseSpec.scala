package usecases

import domains.bot.BotRepository
import domains.workspace.WorkSpaceRepository
import helpers.traits.UseCaseSpec
import infra.DBError
import usecases.UninstallApplicationUseCase.Params

import scala.concurrent.Future

class UninstallApplicationUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val botRepo       = mock[BotRepository]
    val workSpaceRepo = mock[WorkSpaceRepository]

    "succeed" should {
      "invoke botRepository.find once & workSpaceRepository.find once & workSpaceRepository.update once" in {
        forAll(botGen, workSpaceGen) { (bot, workSpace) =>
          val params = Params(bot.id, workSpace.id)

          when(botRepo.find(params.botId))
            .thenReturn(Future.successful(Some(bot)))
          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(workSpace)))
          when(workSpaceRepo.update(workSpace)).thenReturn(Future.unit)

          new UninstallApplicationUseCaseImpl(botRepo, workSpaceRepo)
            .exec(params)
            .futureValue

          verify(botRepo).find(params.botId)
          verify(workSpaceRepo).find(params.workSpaceId)
          verify(workSpaceRepo).update(workSpace)
          reset(botRepo)
        }
      }
    }

    "return None in botRepository.find" should {
      "throw use case error" in {
        forAll(botGen, workSpaceGen) { (bot, workSpace) =>
          val params = Params(bot.id, workSpace.id)

          when(botRepo.find(params.botId)).thenReturn(Future.successful(None))

          val result =
            new UninstallApplicationUseCaseImpl(botRepo, workSpaceRepo)
              .exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === NotFoundError(
                "error while botRepository.find in uninstall bot use case"
              )
            )
            verify(workSpaceRepo, never).find(params.workSpaceId)
            verify(workSpaceRepo, never).update(workSpace)
          }
        }
      }
    }

    "failed in workSpaceRepository.find" should {
      "return future unit" in {
        forAll(botGen, workSpaceGen) { (bot, workSpace) =>
          val params = Params(bot.id, workSpace.id)

          when(botRepo.find(params.botId))
            .thenReturn(Future.successful(Some(bot)))
          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(None))

          val result: Unit = new UninstallApplicationUseCaseImpl(
            botRepo,
            workSpaceRepo
          ).exec(params).futureValue

          assert(result === ())

        }
      }
    }

    "failed in workSpaceRepository.update" should {
      "throw use case error" in {
        forAll(botGen, workSpaceGen) { (bot, workSpace) =>
          val params = Params(bot.id, workSpace.id)

          when(botRepo.find(params.botId))
            .thenReturn(Future.successful(Some(bot)))
          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(workSpace)))
          when(workSpaceRepo.update(workSpace))
            .thenReturn(Future.failed(DBError("error")))

          val result =
            new UninstallApplicationUseCaseImpl(botRepo, workSpaceRepo)
              .exec(params)

          whenReady(result.failed) { e =>
            e === SystemError(
              "error while workSpaceRepository.update in uninstall bot use case" + "\n" + DBError(
                "error"
              ).getMessage
            )
          }
        }
      }
    }
  }

}
