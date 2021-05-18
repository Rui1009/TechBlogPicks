package usecases

import domains.workspace.WorkSpaceRepository
import helpers.traits.UseCaseSpec
import infra.APIError
import usecases.GreetInInvitedChannelUseCase.Params

import scala.concurrent.Future

class GreetInInvitedChannelUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val workSpaceRepo = mock[WorkSpaceRepository]
    "succeed" should {
      "invoke workSpaceRepository.find & workSpaceRepository.sendMessage once" in {
        forAll(
          workSpaceGen,
          applicationIdGen,
          botGen,
          botIdGen,
          channelTypedChannelMessageGen
        ) { (_workSpace, appId, bot, botId, channel) =>
          val params    = Params(_workSpace.id, channel.id, appId, botId)
          val workSpace = _workSpace.copy(
            channels = Seq(channel.copy(id = channel.id)),
            bots = Seq(bot.copy(applicationId = appId, id = Some(botId)))
          )

          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(workSpace)))

          val workSpaceWithUpdatedBot     =
            workSpace.botCreateGreetingInInvitedChannel(params.botId).unsafeGet
          val workSpaceWithUpdatedChannel = workSpaceWithUpdatedBot
            .botPostMessage(params.applicationId, params.channelId)
            .unsafeGet

          when(
            workSpaceRepo.sendMessage(
              workSpaceWithUpdatedChannel,
              params.applicationId,
              params.channelId
            )
          ).thenReturn(Future.successful(Some()))

          new GreetInInvitedChannelUseCaseImpl(workSpaceRepo)
            .exec(params)
            .futureValue

          verify(workSpaceRepo).find(params.workSpaceId)
          verify(workSpaceRepo).sendMessage(
            workSpaceWithUpdatedChannel,
            params.applicationId,
            params.channelId
          )

          reset(workSpaceRepo)
        }
      }
    }

    "return Left in botCreateGreetingInInvitedChannel" should {
      "retun unit & never invoke workSpaceRepository.sendMessage" in {
        forAll(
          workSpaceGen,
          applicationIdGen,
          botGen,
          botIdGen,
          channelTypedChannelMessageGen
        ) { (_workSpace, appId, bot, botId, channel) =>
          val params    = Params(_workSpace.id, channel.id, appId, botId)
          val workSpace = _workSpace.copy(
            channels = Seq(channel.copy(id = channel.id)),
            bots = Seq(bot.copy(applicationId = appId))
          )

          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(workSpace)))

          val result = new GreetInInvitedChannelUseCaseImpl(workSpaceRepo)
            .exec(params)
            .futureValue

          verify(workSpaceRepo).find(params.workSpaceId)
          verify(workSpaceRepo, times(0)).sendMessage(*, *, *)
          assert(result === ())

          reset(workSpaceRepo)
        }
      }
    }

    "return None in workSpaceRepository.find" should {
      "throw use case error & workSpaceRepository.sendMessage never invokes" in {
        forAll(
          workSpaceGen,
          applicationIdGen,
          botIdGen,
          channelTypedChannelMessageGen
        ) { (workSpace, appId, botId, channel) =>
          val params = Params(workSpace.id, channel.id, appId, botId)

          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(None))

          val result =
            new GreetInInvitedChannelUseCaseImpl(workSpaceRepo).exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === NotFoundError(
                "error while workSpaceRepository.find in greet in invited channel use case"
              )
            )
            verify(workSpaceRepo).find(params.workSpaceId)
            verify(workSpaceRepo, times(0)).sendMessage(*, *, *)

            reset(workSpaceRepo)
          }
        }
      }
    }

    "failed in workSpaceRepository.sendMessage" should {
      "throw use case error" in {
        forAll(
          workSpaceGen,
          applicationIdGen,
          botGen,
          botIdGen,
          channelTypedChannelMessageGen
        ) { (_workSpace, appId, bot, botId, channel) =>
          val params    = Params(_workSpace.id, channel.id, appId, botId)
          val workSpace = _workSpace.copy(
            channels = Seq(channel.copy(id = channel.id)),
            bots = Seq(bot.copy(applicationId = appId, id = Some(botId)))
          )

          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(workSpace)))

          val workSpaceWithUpdatedBot     =
            workSpace.botCreateGreetingInInvitedChannel(params.botId).unsafeGet
          val workSpaceWithUpdatedChannel = workSpaceWithUpdatedBot
            .botPostMessage(params.applicationId, params.channelId)
            .unsafeGet

          when(
            workSpaceRepo.sendMessage(
              workSpaceWithUpdatedChannel,
              params.applicationId,
              params.channelId
            )
          ).thenReturn(Future.failed(APIError("error")))

          val result =
            new GreetInInvitedChannelUseCaseImpl(workSpaceRepo).exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === SystemError(
                "error while workSpaceRepository.sendMessage in greet in invited channel use case" +
                  APIError("error").getMessage
              )
            )
            reset(workSpaceRepo)
          }
        }
      }
    }
  }
}
