package usecases

import domains.application.Application.{
  ApplicationClientId,
  ApplicationClientSecret
}
import domains.application.ApplicationRepository
import domains.bot.Bot.BotAccessToken
import domains.workspace.WorkSpaceRepository
import eu.timepit.refined.auto._
import helpers.traits.UseCaseSpec
import infra.DBError
import usecases.InstallApplicationUseCase._

import scala.concurrent.Future

class InstallApplicationUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val workSpaceRepo   = mock[WorkSpaceRepository]
    val applicationRepo = mock[ApplicationRepository]
    "succeed" should {
      "invoke applicationRepository.find once & workSpaceRepository.find once & workSpaceRepository.update once" in {
        forAll(
          temporaryOauthCodeGen,
          workSpaceGen,
          applicationGen,
          applicationClientIdGen,
          applicationClientSecretGen
        ) { (tempOauthCode, _workSpace, _application, clientId, clientSecret) =>
          val application     = _application
            .copy(clientId = Some(clientId), clientSecret = Some(clientSecret))
          val params          = Params(tempOauthCode, application.id)
          val targetWorkSpace =
            _workSpace.copy(unallocatedToken = Some(BotAccessToken("test")))

          when(applicationRepo.find(params.applicationId))
            .thenReturn(Future.successful(Some(application)))
          when(
            workSpaceRepo.find(
              params.temporaryOauthCode,
              application.clientId.get,
              application.clientSecret.get
            )
          ).thenReturn(Future.successful(targetWorkSpace))
          when(
            workSpaceRepo.update(
              targetWorkSpace.installApplication(application).unsafeGet,
              application.id
            )
          ).thenReturn(Future.successful(Some(())))

          new InstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
            .exec(params)
            .futureValue

          verify(applicationRepo).find(params.applicationId)
          verify(workSpaceRepo).find(
            params.temporaryOauthCode,
            application.clientId.get,
            application.clientSecret.get
          )
          verify(workSpaceRepo).update(
            targetWorkSpace.installApplication(application).unsafeGet,
            application.id
          )
          reset(workSpaceRepo)
          reset(applicationRepo)
        }
      }
    }

    "return None in applicationRepository.find" should {
      "throw use case error and not invoked workSpaceRepository.find & workSpaceRepository.update" in {
        forAll(temporaryOauthCodeGen, applicationGen) {
          (tempOauthCode, _application) =>
            val params = Params(tempOauthCode, _application.id)

            when(applicationRepo.find(params.applicationId))
              .thenReturn(Future.successful(None))

            val result =
              new InstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
                .exec(params)

            whenReady(result.failed) { e =>
              assert(
                e === NotFoundError(
                  "error while applicationRepository.find in install application use case"
                )
              )
            }
        }
      }
    }

    "returned clientId which is None" should {
      "throw use case error and not invoked workSpaceRepository.find and workSpaceRepository.update" in {
        forAll(temporaryOauthCodeGen, applicationGen) {
          (tempOauthCode, application) =>
            val params = Params(tempOauthCode, application.id)

            when(applicationRepo.find(params.applicationId)).thenReturn(
              Future.successful(
                Some(
                  application.updateClientInfo(
                    None,
                    Some(ApplicationClientSecret("test"))
                  )
                )
              )
            )

            val result =
              new InstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
                .exec(params)

            whenReady(result.failed) { e =>
              assert(
                e === SystemError(
                  "error while get application client id in install application use case"
                )
              )
            }
        }
      }
    }

    "returned clientSecret which is None" should {
      "throw use case error and not invoked workSpaceRepository.find and workSpaceRepository.update" in {
        forAll(temporaryOauthCodeGen, applicationGen) {
          (tempOauthCode, application) =>
            val params = Params(tempOauthCode, application.id)

            when(applicationRepo.find(params.applicationId)).thenReturn(
              Future.successful(
                Some(
                  application
                    .updateClientInfo(Some(ApplicationClientId("test")), None)
                )
              )
            )

            val result =
              new InstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
                .exec(params)

            whenReady(result.failed) { e =>
              assert(
                e === SystemError(
                  "error while get application client secret in install application use case"
                )
              )
            }
        }
      }
    }

    "failed in workSpaceRepository.find" should {
      "throw use case error and not invoked workSpaceRepository.update" in {
        forAll(temporaryOauthCodeGen, applicationGen) {
          (tempOauthCode, _application) =>
            val application = _application.updateClientInfo(
              Some(ApplicationClientId("test")),
              Some(ApplicationClientSecret("test"))
            )
            val params      = Params(tempOauthCode, application.id)

            when(applicationRepo.find(params.applicationId))
              .thenReturn(Future.successful(Some(application)))
            when(
              workSpaceRepo.find(
                params.temporaryOauthCode,
                ApplicationClientId("test"),
                ApplicationClientSecret("test")
              )
            ).thenReturn(Future.failed(DBError("error")))

            val result =
              new InstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
                .exec(params)

            whenReady(result.failed) { e =>
              assert(
                e === SystemError(
                  "error while workSpaceRepository.find in install application use case"
                    + "\n" + DBError("error").getMessage
                )
              )
            }
            reset(workSpaceRepo)
        }
      }
    }

    "return Left in WorkSpace.installApplication" should {
      "return unit & never invoke workSpaceRepository.update" in {
        forAll(temporaryOauthCodeGen, applicationGen, workSpaceGen, botGen) {
          (code, _application, workSpace, bot) =>
            val application = _application.updateClientInfo(
              Some(ApplicationClientId("cid")),
              Some(ApplicationClientSecret("cs"))
            )
            val params      = Params(code, application.id)

            val targetWorkSpace = workSpace.copy(
              unallocatedToken = Some(BotAccessToken("token")),
              bots = Seq(bot.copy(applicationId = application.id))
            )

            when(applicationRepo.find(params.applicationId))
              .thenReturn(Future.successful(Some(application)))
            when(
              workSpaceRepo.find(
                params.temporaryOauthCode,
                ApplicationClientId("cid"),
                ApplicationClientSecret("cs")
              )
            ).thenReturn(Future.successful(targetWorkSpace))

            val result = new InstallApplicationUseCaseImpl(
              workSpaceRepo,
              applicationRepo
            ).exec(params).futureValue

            assert(result === ())
            verify(workSpaceRepo, times(0)).update(*, *)

            reset(workSpaceRepo)
        }
      }
    }

    "failed in workSpaceRepository.update" should {
      "throw use case error" in {
        forAll(temporaryOauthCodeGen, applicationGen, workSpaceGen) {
          (tempOauthCode, _application, workSpace) =>
            val application     = _application.updateClientInfo(
              Some(ApplicationClientId("test")),
              Some(ApplicationClientSecret("test"))
            )
            val params          = Params(tempOauthCode, application.id)
            val targetWorkSpace =
              workSpace.copy(unallocatedToken = Some(BotAccessToken("test")))

            when(applicationRepo.find(params.applicationId))
              .thenReturn(Future.successful(Some(application)))
            when(
              workSpaceRepo.find(
                params.temporaryOauthCode,
                ApplicationClientId("test"),
                ApplicationClientSecret("test")
              )
            ).thenReturn(Future.successful(targetWorkSpace))
            when(
              workSpaceRepo.update(
                targetWorkSpace.installApplication(application).unsafeGet,
                application.id
              )
            ).thenReturn(Future.failed(DBError("error")))

            val result =
              new InstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
                .exec(params)

            whenReady(result.failed) { e =>
              assert(
                e === SystemError(
                  "error while workSpaceRepository.update in install application use case"
                    + DBError("error").getMessage
                )
              )
            }
        }
      }
    }
  }
}
