package usecases

import domains.workspace.WorkSpaceRepository
import helpers.traits.UseCaseSpec
import infra.{APIError, DBError}
import org.postgresql.ssl.DbKeyStoreSocketFactory.DbKeyStoreSocketException
import usecases.JoinChannelUseCase.Params

import scala.concurrent.Future

class JoinChannelUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val workSpaceRepo = mock[WorkSpaceRepository]

    "succeed" should {
      "invoke workSpaceRepository.find & workSpaceRepository.joinChannels once" in {
        forAll(
          applicationIdGen,
          channelTypedChannelMessageGen,
          workSpaceIdGen,
          workSpaceGen,
          botGen
        ) { (appId, channel, workspaceId, _workSpace, bot) =>
          val params           = Params(channel.id, appId, workspaceId)
          val workSpace        = _workSpace.copy(
            id = workspaceId,
            bots = _workSpace.bots :+ bot.copy(applicationId = appId),
            channels = _workSpace.channels :+ channel
          )
          val updatedWorkSpace = workSpace
            .addBotToChannel(params.applicationId, params.channelId)
            .unsafeGet

          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(workSpace)))
          when(
            workSpaceRepo.joinChannels(
              updatedWorkSpace,
              params.applicationId,
              Seq(params.channelId)
            )
          ).thenReturn(Future.unit)

          new JoinChannelUseCaseImpl(workSpaceRepo).exec(params).futureValue

          verify(workSpaceRepo).find(workspaceId)
          verify(workSpaceRepo).joinChannels(
            updatedWorkSpace,
            params.applicationId,
            Seq(params.channelId)
          )
          reset(workSpaceRepo)
        }
      }
    }

    "find return None" should {
      "return use case error & never invoke workSpaceRepository.joinChannels" in {
        forAll(applicationIdGen, channelIdGen, workSpaceIdGen) {
          (appId, channelId, workSpaceId) =>
            val params = Params(channelId, appId, workSpaceId)

            when(workSpaceRepo.find(params.workSpaceId))
              .thenReturn(Future.successful(None))

            val result = new JoinChannelUseCaseImpl(workSpaceRepo).exec(params)

            whenReady(result.failed)(e =>
              assert(
                e === NotFoundError(
                  "error while workSpaceRepository.find in join channel use case"
                )
              )
            )
            verify(workSpaceRepo, never).joinChannels(*, *, *)
            reset(workSpaceRepo)
        }
      }
    }

    "joinChannels return fail" should {
      "return use case error" in {
        forAll(
          applicationIdGen,
          channelTypedChannelMessageGen,
          workSpaceIdGen,
          workSpaceGen,
          botGen
        ) { (appId, channel, workspaceId, _workSpace, bot) =>
          val params           = Params(channel.id, appId, workspaceId)
          val workSpace        = _workSpace.copy(
            id = workspaceId,
            bots = _workSpace.bots :+ bot.copy(applicationId = appId),
            channels = _workSpace.channels :+ channel
          )
          val updatedWorkSpace = workSpace
            .addBotToChannel(params.applicationId, params.channelId)
            .unsafeGet

          when(workSpaceRepo.find(params.workSpaceId))
            .thenReturn(Future.successful(Some(workSpace)))
          when(
            workSpaceRepo.joinChannels(
              updatedWorkSpace,
              params.applicationId,
              Seq(params.channelId)
            )
          ).thenReturn(Future.failed(DBError("error")))

          val result = new JoinChannelUseCaseImpl(workSpaceRepo).exec(params)

          whenReady(result.failed)(e =>
            assert(
              e === SystemError(
                "error while workSpaceRepository.joinChannels in join channel use case" + "\n"
                  + DBError("error").getMessage
              )
            )
          )
        }
      }
    }
  }
}
