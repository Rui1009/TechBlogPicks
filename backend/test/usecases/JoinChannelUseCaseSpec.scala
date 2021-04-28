package usecases

import domains.workspace.WorkSpaceRepository
import helpers.traits.UseCaseSpec
import infra.APIError
import usecases.JoinChannelUseCase.Params

import scala.concurrent.Future

class JoinChannelUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val workSpaceRepo = mock[WorkSpaceRepository]

    "succeed" should {
      "invoke workSpaceRepository.find & workSpaceRepository.update once" in {
        forAll(applicationIdGen, channelIdGen, workSpaceIdGen, workSpaceGen) {
          (appId, channelId, workspaceId, _workSpace) =>
            val params           = Params(channelId, appId, workspaceId)
            val workSpace        = _workSpace.copy(id = workspaceId)
            val updatedWorkSpace =
              workSpace.addBotToChannel(appId, channelId).unsafeGet

            when(workSpaceRepo.find(params.workSpaceId))
              .thenReturn(Future.successful(Some(workSpace)))
            when(workSpaceRepo.update(updatedWorkSpace)).thenReturn(Future.unit)

            new JoinChannelUseCaseImpl(workSpaceRepo).exec(params).futureValue

            verify(workSpaceRepo).find(workspaceId)
            verify(workSpaceRepo).update(updatedWorkSpace)
            reset(workSpaceRepo)
        }
      }
    }

    "find return None" should {
      "return use case error & never invoke workSpaceRepository.update" in {
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
            verify(workSpaceRepo, never).update(*)
            reset(workSpaceRepo)
        }
      }
    }

    "update return fail" should {
      "return use case error" in {
        forAll(applicationIdGen, channelIdGen, workSpaceIdGen, workSpaceGen) {
          (appId, channelId, workspaceId, _workSpace) =>
            val params           = Params(channelId, appId, workspaceId)
            val workSpace        = _workSpace.copy(id = workspaceId)
            val updatedWorkSpace =
              workSpace.addBotToChannel(appId, channelId).unsafeGet

            when(workSpaceRepo.find(params.workSpaceId))
              .thenReturn(Future.successful(Some(workSpace)))
            when(workSpaceRepo.update(updatedWorkSpace))
              .thenReturn(Future.failed(APIError("error")))

            val result = new JoinChannelUseCaseImpl(workSpaceRepo).exec(params)

            whenReady(result.failed)(e =>
              assert(
                e === SystemError(
                  "error while workSpaceRepository.update in join channel use case" + "\n"
                    + APIError("error").getMessage
                )
              )
            )
        }
      }
    }
  }
}
