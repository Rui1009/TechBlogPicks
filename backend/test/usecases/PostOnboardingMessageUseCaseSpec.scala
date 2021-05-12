package usecases

import domains.workspace.WorkSpaceRepository
import helpers.traits.UseCaseSpec
import usecases.PostOnboardingMessageUseCase.Params
import eu.timepit.refined.auto._
import infra.{APIError, DBError}

import scala.concurrent.Future

class PostOnboardingMessageUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val workSpaceRepo = mock[WorkSpaceRepository]

    "succeed when no channel messages exist" should {
      "invoke workSpaceRepository.find & workSpaceRepository.sendMessage once" in {
        forAll(
          workSpaceGen,
          applicationIdGen,
          botGen,
          channelTypedChannelMessageGen
        ) { (workSpace, appId, bot, channel) =>
          val params            = Params(appId, workSpace.id, channel.id)
          val returnedWorkSpace = workSpace.copy(
            channels = Seq(channel.copy(id = channel.id, history = Seq())),
            bots = Seq(bot.copy(applicationId = appId))
          )
          val targetChannel     =
            returnedWorkSpace.findChannel(params.channelId).unsafeGet

          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(returnedWorkSpace)))

          val workSpaceWithUpdatedBots     = returnedWorkSpace
            .botCreateOnboardingMessage(params.applicationId)
            .unsafeGet
          val workSpaceWithUpdatedChannels = workSpaceWithUpdatedBots
            .botPostMessage(params.applicationId, targetChannel.id)
            .unsafeGet

          when(
            workSpaceRepo.sendMessage(
              workSpaceWithUpdatedChannels,
              params.applicationId,
              params.channelId
            )
          ).thenReturn(Future.successful(Some()))

          new PostOnboardingMessageUseCaseImpl(workSpaceRepo)
            .exec(params)
            .futureValue

          verify(workSpaceRepo).find(params.workSpaceId)
          verify(workSpaceRepo).sendMessage(
            workSpaceWithUpdatedChannels,
            params.applicationId,
            params.channelId
          )

          reset(workSpaceRepo)
        }
      }
    }

    "succeed when any channel message exists" should {
      "invoke workSpaceRepository.find & workSpaceRepository.sendMessage never invokes" in {
        forAll(
          workSpaceGen,
          applicationIdGen,
          botGen,
          channelTypedChannelMessageGen
        ) { (workSpace, appId, bot, channel) =>
          val params            = Params(appId, workSpace.id, channel.id)
          val returnedWorkSpace = workSpace.copy(
            channels = Seq(channel.copy(id = channel.id)),
            bots = Seq(bot.copy(applicationId = appId))
          )
          val targetChannel     =
            returnedWorkSpace.findChannel(params.channelId).unsafeGet

          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(returnedWorkSpace)))

          new PostOnboardingMessageUseCaseImpl(workSpaceRepo)
            .exec(params)
            .futureValue

          verify(workSpaceRepo).find(params.workSpaceId)
          verify(workSpaceRepo, times(0)).sendMessage(*, *, *)

          reset(workSpaceRepo)
        }
      }
    }

    "return None in workSpaceRepository.find" should {
      "throw use case error & workSpaceRepository.sendMessage never invokes" in {
        forAll(workSpaceGen, applicationIdGen, channelTypedChannelMessageGen) {
          (workSpace, appId, channel) =>
            val params = Params(appId, workSpace.id, channel.id)

            when(workSpaceRepo.find(params.workSpaceId))
              .thenReturn(Future.successful(None))

            val result =
              new PostOnboardingMessageUseCaseImpl(workSpaceRepo).exec(params)

            whenReady(result.failed) { e =>
              assert(
                e === NotFoundError(
                  "error while workSpaceRepository.find in post onboarding message use case"
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
        ) { (workSpace, appId, bot, channel) =>
          val params            = Params(appId, workSpace.id, channel.id)
          val returnedWorkSpace = workSpace.copy(
            channels = Seq(channel.copy(id = channel.id, history = Seq())),
            bots = Seq(bot.copy(applicationId = appId))
          )
          val targetChannel     =
            returnedWorkSpace.findChannel(params.channelId).unsafeGet

          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(returnedWorkSpace)))
          val workSpaceWithUpdatedBots     = returnedWorkSpace
            .botCreateOnboardingMessage(params.applicationId)
            .unsafeGet
          val workSpaceWithUpdatedChannels = workSpaceWithUpdatedBots
            .botPostMessage(params.applicationId, targetChannel.id)
            .unsafeGet

          when(
            workSpaceRepo.sendMessage(
              workSpaceWithUpdatedChannels,
              params.applicationId,
              params.channelId
            )
          ).thenReturn(Future.failed(APIError("error")))

          val result =
            new PostOnboardingMessageUseCaseImpl(workSpaceRepo).exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === SystemError(
                "error while workSpaceRepository.sendMessage in post onboarding message use case" +
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
