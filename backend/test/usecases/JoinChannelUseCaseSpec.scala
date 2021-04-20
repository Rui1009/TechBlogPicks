package usecases

import domains.bot.BotRepository
import helpers.traits.UseCaseSpec
import infra.DBError
import usecases.JoinChannelUseCase.Params

import scala.concurrent.Future

class JoinChannelUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val botRepo = mock[BotRepository]

    "succeed" should {
      "invoke botRepository.find & botRepository.join once" in {
        forAll(botGen, channelIdGen, workSpaceIdGen) {
          (bot, channelId, workspaceId) =>
            val params = Params(channelId, bot.id, workspaceId)

            when(botRepo.find(params.botId, params.workSpaceId))
              .thenReturn(Future.successful(Some(bot)))
            when(botRepo.join(bot.joinTo(params.channelId)))
              .thenReturn(Future.unit)

            new JoinChannelUseCaseImpl(botRepo).exec(params).futureValue

            verify(botRepo).find(params.botId, params.workSpaceId)
            verify(botRepo).join(bot.joinTo(params.channelId))
            reset(botRepo)
        }
      }
    }

    "find return None" should {
      "return use case error & never invoke botRepository.join" in {
        forAll(botGen, channelIdGen, workSpaceIdGen) {
          (bot, channelId, workSpaceId) =>
            val params = Params(channelId, bot.id, workSpaceId)

            when(botRepo.find(params.botId, params.workSpaceId))
              .thenReturn(Future.successful(None))

            val result = new JoinChannelUseCaseImpl(botRepo).exec(params)

            whenReady(result.failed)(e =>
              assert(
                e === NotFoundError(
                  "error while botRepository.find in join channel use case"
                )
              )
            )
            verify(botRepo, times(0)).join(bot.joinTo(params.channelId))
            reset(botRepo)
        }
      }
    }
  }
}
