package usecases

import domains.message.MessageRepository
import domains.workspace.WorkSpace.WorkSpaceToken
import domains.workspace.WorkSpaceRepository
import helpers.traits.UseCaseSpec
import usecases.PostOnboardingMessageUseCase.Params
import eu.timepit.refined.auto._

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
  }
}
