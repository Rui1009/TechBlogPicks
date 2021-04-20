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
        forAll(botGen, channelIdGen) { (bot, channelId) =>
          val params = Params(channelId, bot.id)

          when(botRepo.find(params.botId))
            .thenReturn(Future.successful(Some(bot)))
          when(botRepo.join(bot.joinTo(params.channelId)))
            .thenReturn(Future.unit)

          new JoinChannelUseCaseImpl(botRepo).exec(params).futureValue

          verify(botRepo).find(params.botId)
          verify(botRepo).join(bot.joinTo(params.channelId))
          reset(botRepo)
        }
      }
    }

    "find return None" should {
      "return use case error & never invoke botRepository.join" in {
        forAll(botGen, channelIdGen) { (bot, channelId) =>
          val params = Params(channelId, bot.id)

          when(botRepo.find(params.botId)).thenReturn(Future.successful(None))

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
