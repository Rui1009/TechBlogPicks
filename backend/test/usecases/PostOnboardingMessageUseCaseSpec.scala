package usecases

import domains.message.MessageRepository
import domains.workspace.WorkSpace.WorkSpaceToken
import domains.workspace.WorkSpaceRepository
import helpers.traits.UseCaseSpec
import usecases.PostOnboardingMessageUseCase.Params
import eu.timepit.refined.auto._
import infra.DBError
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.Future

class PostOnboardingMessageUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val workSpaceRepo = mock[WorkSpaceRepository]
    val messageRepo   = mock[MessageRepository]

    "succeed" should {
      "invoke workSpaceRepository.find & messageRepository.isEmpty & messageRepository.add once" in {
        forAll(workSpaceGen, botGen, messageGen) { (workSpace, bot, message) =>
          val params = Params(bot.id, workSpace.id, message.channelId)

          val targetToken = WorkSpaceToken("mockToken")
          when(workSpaceRepo.find(params.workSpaceId, params.botId)).thenReturn(
            Future.successful(Some(workSpace.copy(tokens = Seq(targetToken))))
          )
          when(messageRepo.isEmpty(targetToken, params.channelId))
            .thenReturn(Future.successful(true))
          when(messageRepo.add(targetToken, params.channelId, Seq()))
            .thenReturn(Future.successful(()))

          new PostOnboardingMessageUseCaseImpl(workSpaceRepo, messageRepo)
            .exec(params)
            .futureValue

          verify(workSpaceRepo).find(params.workSpaceId, params.botId)
          verify(messageRepo).isEmpty(targetToken, params.channelId)
          verify(messageRepo).add(targetToken, params.channelId, Seq())
          reset(workSpaceRepo)
          reset(messageRepo)
        }
      }
    }

    "return None in workSpaceRepository.find" should {
      "throw use case error" in {
        forAll(workSpaceGen, botGen, messageGen) { (workSpace, bot, message) =>
          val params = Params(bot.id, workSpace.id, message.channelId)

          when(workSpaceRepo.find(params.workSpaceId, params.botId))
            .thenReturn(Future.successful(None))

          val result =
            new PostOnboardingMessageUseCaseImpl(workSpaceRepo, messageRepo)
              .exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === NotFoundError(
                "error while workSpaceRepository.find in post onboarding message use case"
              )
            )
          }
          reset(workSpaceRepo)
          reset(messageRepo)
        }
      }
    }

    "failed in messageRepository.isEmpty" should {
      "throw use case error" in {
        forAll(workSpaceGen, botGen, messageGen) { (workSpace, bot, message) =>
          val params = Params(bot.id, workSpace.id, message.channelId)

          val targetToken = WorkSpaceToken("mockToken")
          when(workSpaceRepo.find(params.workSpaceId, params.botId)).thenReturn(
            Future.successful(Some(workSpace.copy(tokens = Seq(targetToken))))
          )
          when(messageRepo.isEmpty(targetToken, params.channelId))
            .thenReturn(Future.failed(DBError("error")))

          val result =
            new PostOnboardingMessageUseCaseImpl(workSpaceRepo, messageRepo)
              .exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === SystemError(
                "error while messageRepository.isEmpty in post onboarding message use case" + "\n" + DBError(
                  "error"
                ).getMessage
              )
            )
          }
          reset(workSpaceRepo)
          reset(messageRepo)
        }
      }
    }

    "failed in messageRepository.add" should {
      "throw use case error" in {
        forAll(workSpaceGen, botGen, messageGen) { (workSpace, bot, message) =>
          val params = Params(bot.id, workSpace.id, message.channelId)

          val targetToken = WorkSpaceToken("mockToken")
          when(workSpaceRepo.find(params.workSpaceId, params.botId)).thenReturn(
            Future.successful(Some(workSpace.copy(tokens = Seq(targetToken))))
          )
          when(messageRepo.isEmpty(targetToken, params.channelId))
            .thenReturn(Future.successful(true))
          when(messageRepo.add(targetToken, params.channelId, Seq()))
            .thenReturn(Future.failed(DBError("error")))

          val result =
            new PostOnboardingMessageUseCaseImpl(workSpaceRepo, messageRepo)
              .exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === SystemError(
                "error while messageRepository.add in post onboarding message use case" + "\n" + DBError(
                  "error"
                ).getMessage
              )
            )
          }
        }
      }
    }

    "return false in messageRepository.isEmpty" should {
      "return future unit without exec messageRepository.add" in {
        forAll(workSpaceGen, botGen, messageGen) { (workSpace, bot, message) =>
          val params = Params(bot.id, workSpace.id, message.channelId)

          val targetToken = WorkSpaceToken("mockToken")
          when(workSpaceRepo.find(params.workSpaceId, params.botId)).thenReturn(
            Future.successful(Some(workSpace.copy(tokens = Seq(targetToken))))
          )
          when(messageRepo.isEmpty(targetToken, params.channelId))
            .thenReturn(Future.successful(false))

          val result: Unit = new PostOnboardingMessageUseCaseImpl(
            workSpaceRepo,
            messageRepo
          ).exec(params).futureValue

          verify(workSpaceRepo).find(params.workSpaceId, params.botId)
          verify(messageRepo).isEmpty(targetToken, params.channelId)
          verify(messageRepo, times(0))
            .add(targetToken, params.channelId, Seq())
          assert(result === ())
          reset(workSpaceRepo)
          reset(messageRepo)
        }
      }
    }
  }
}
