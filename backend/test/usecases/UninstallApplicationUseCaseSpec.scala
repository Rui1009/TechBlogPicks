package usecases

import domains.application.ApplicationRepository
import domains.workspace.WorkSpaceRepository
import helpers.traits.UseCaseSpec
import infra.DBError
import usecases.UninstallApplicationUseCase.Params

import scala.concurrent.Future

class UninstallApplicationUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val applicationRepo = mock[ApplicationRepository]
    val workSpaceRepo   = mock[WorkSpaceRepository]

    "succeed" should {
      "invoke applicationRepository.find once & workSpaceRepository.find once & workSpaceRepository.removeBot once" in {
        forAll(applicationGen, workSpaceGen) { (application, workSpace) =>
          val params = Params(workSpace.id, application.id)

          when(applicationRepo.find(params.applicationId))
            .thenReturn(Future.successful(Some(application)))
          when(workSpaceRepo.findByConstToken(params.workSpaceId))
            .thenReturn(Future.successful(Some(workSpace)))

          val updatedWorkSpace = workSpace.uninstallApplication(application)
          when(workSpaceRepo.removeBot(updatedWorkSpace))
            .thenReturn(Future.successful(()))

          new UninstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
            .exec(params)
            .futureValue

          verify(applicationRepo).find(params.applicationId)
          verify(workSpaceRepo).findByConstToken(params.workSpaceId)
          verify(workSpaceRepo).removeBot(updatedWorkSpace)
          reset(applicationRepo)
          reset(workSpaceRepo)
        }
      }
    }

    "no application exists in applicationRepository.find" should {
      "throw use case error & workSpaceRepository.find & workSpaceRepository.removeBot not invoked" in {
        forAll(workSpaceGen, applicationGen) { (workSpace, application) =>
          val params = Params(workSpace.id, application.id)

          when(applicationRepo.find(params.applicationId))
            .thenReturn(Future.successful(None))

          val result =
            new UninstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
              .exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === NotFoundError(
                "error while applicationRepository.find in uninstall application use case"
              )
            )
            verify(workSpaceRepo, times(0)).find(*)
            verify(workSpaceRepo, times(0)).removeBot(*)
          }
        }
      }
    }

    "no workSpace exists in workSpaceRepository.find" should {
      "never throw use case error & return unit & not invoked workSpaceRepository.removeBot" in {
        forAll(workSpaceGen, applicationGen) { (workSpace, application) =>
          val params = Params(workSpace.id, application.id)

          when(applicationRepo.find(params.applicationId))
            .thenReturn(Future.successful(Some(application)))
          when(workSpaceRepo.findByConstToken(params.workSpaceId))
            .thenReturn(Future.successful(None))

          val result = new UninstallApplicationUseCaseImpl(
            workSpaceRepo,
            applicationRepo
          ).exec(params).futureValue

          verify(workSpaceRepo, times(0)).removeBot(*)
          assert(result === ())
        }
      }
    }

    "failed in workSpaceRepository.removeBot" should {
      "throw use case error" in {
        forAll(workSpaceGen, applicationGen) { (workSpace, application) =>
          val params = Params(workSpace.id, application.id)

          when(applicationRepo.find(params.applicationId))
            .thenReturn(Future.successful(Some(application)))
          when(workSpaceRepo.findByConstToken(params.workSpaceId))
            .thenReturn(Future.successful(Some(workSpace)))

          val updatedWorkSpace = workSpace.uninstallApplication(application)
          when(workSpaceRepo.removeBot(updatedWorkSpace))
            .thenReturn(Future.failed(DBError("error")))

          val result =
            new UninstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
              .exec(params)

          val msg = """
              |SystemError
              |error while workSpaceRepository.removeBot in uninstall application use case
              |DBError
              |error
              |""".stripMargin.trim

          whenReady(result.failed)(e => assert(e.getMessage.trim === msg))
        }
      }
    }
  }
}
