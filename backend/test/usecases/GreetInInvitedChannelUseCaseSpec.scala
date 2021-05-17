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
          channelTypedChannelMessageGen
        ) { (_workSpace, appId, bot, channel) =>
          val params    = Params(_workSpace.id, channel.id, appId)
          val workSpace = _workSpace.copy(
            channels = Seq(channel.copy(id = channel.id)),
            bots = Seq(bot.copy(applicationId = appId))
          )

          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(workSpace)))

          val workSpaceWithUpdatedBot     = workSpace
            .botCreateGreetingInInvitedChannel(params.applicationId)
            .unsafeGet
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

    "return None in workSpaceRepository.find" should {
      "throw use case error & workSpaceRepository.sendMessage never invokes" in {
        forAll(workSpaceGen, applicationIdGen, channelTypedChannelMessageGen) {
          (workSpace, appId, channel) =>
            val params = Params(workSpace.id, channel.id, appId)

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
          channelTypedChannelMessageGen
        ) { (_workSpace, appId, bot, channel) =>
          val params    = Params(_workSpace.id, channel.id, appId)
          val workSpace = _workSpace.copy(
            channels = Seq(channel.copy(id = channel.id)),
            bots = Seq(bot.copy(applicationId = appId))
          )

          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(workSpace)))

          val workSpaceWithUpdatedBot     = workSpace
            .botCreateGreetingInInvitedChannel(params.applicationId)
            .unsafeGet
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
