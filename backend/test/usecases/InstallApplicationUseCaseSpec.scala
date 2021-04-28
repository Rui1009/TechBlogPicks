//package usecases
//
//import domains.application.Application.{
//  ApplicationClientId,
//  ApplicationClientSecret
//}
//import domains.application.ApplicationRepository
//import domains.workspace.WorkSpaceRepository
//import eu.timepit.refined.auto._
//import helpers.traits.UseCaseSpec
//import infra.DBError
//import usecases.InstallApplicationUseCase._
//
//import scala.concurrent.Future
//
//class InstallApplicationUseCaseSpec extends UseCaseSpec {
//  "exec" when {
//    val workSpaceRepo   = mock[WorkSpaceRepository]
//    val applicationRepo = mock[ApplicationRepository]
//    "succeed" should {
//      "invoke applicationRepository.find once & workSpaceRepository.find once & workSpaceRepository.update once" in {
//        forAll(
//          temporaryOauthCodeGen,
//          workSpaceGen,
//          applicationGen,
//          applicationClientIdGen,
//          applicationClientSecretGen
//        ) { (tempOauthCode, _workSpace, _application, clientId, clientSecret) =>
//          val application = _application
//            .copy(clientId = Some(clientId), clientSecret = Some(clientSecret))
//          val params      = Params(tempOauthCode, application.id)
//
//          when(applicationRepo.find(params.applicationId))
//            .thenReturn(Future.successful(Some(application)))
//          when(
//            workSpaceRepo.find(
//              params.temporaryOauthCode,
//              application.clientId.get,
//              application.clientSecret.get
//            )
//          ).thenReturn(Future.successful(Some(_workSpace)))
//          when(
//            workSpaceRepo.update(
//              _workSpace.installApplication(application).unsafeGet,
//              application.id
//            )
//          ).thenReturn(Future.successful(Some(Unit)))
//
//          new InstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
//            .exec(params)
//            .futureValue
//
//          verify(applicationRepo).find(params.applicationId)
//          verify(workSpaceRepo).find(
//            params.temporaryOauthCode,
//            application.clientId.get,
//            application.clientSecret.get
//          )
//          verify(workSpaceRepo).update(
//            _workSpace.installApplication(application).unsafeGet,
//            application.id
//          )
//          reset(workSpaceRepo)
//          reset(applicationRepo)
//        }
//      }
//    }
//
//    "return None in applicationRepository.find" should {
//      "throw use case error and not invoked workSpaceRepository.find & workSpaceRepository.update" in {
//        forAll(temporaryOauthCodeGen, applicationGen) {
//          (tempOauthCode, _application) =>
//            val params = Params(tempOauthCode, _application.id)
//
//            when(applicationRepo.find(params.applicationId))
//              .thenReturn(Future.successful(None))
//
//            val result =
//              new InstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
//                .exec(params)
//
//            whenReady(result.failed) { e =>
//              assert(
//                e === NotFoundError(
//                  "error while applicationRepository.find in install application use case"
//                )
//              )
//            }
//        }
//      }
//    }
//
//    "returned clientId which is None" should {
//      "throw use case error and not invoked workSpaceRepository.find and workSpaceRepository.update" in {
//        forAll(temporaryOauthCodeGen, applicationGen) {
//          (tempOauthCode, application) =>
//            val params = Params(tempOauthCode, application.id)
//
//            when(applicationRepo.find(params.applicationId)).thenReturn(
//              Future.successful(
//                Some(
//                  application.updateClientInfo(
//                    None,
//                    Some(ApplicationClientSecret("test"))
//                  )
//                )
//              )
//            )
//
//            val result =
//              new InstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
//                .exec(params)
//
//            whenReady(result.failed) { e =>
//              assert(
//                e === SystemError(
//                  "error while get application client id in install application use case"
//                )
//              )
//            }
//        }
//      }
//    }
//
//    "returned clientSecret which is None" should {
//      "throw use case error and not invoked workSpaceRepository.find and workSpaceRepository.update" in {
//        forAll(temporaryOauthCodeGen, applicationGen) {
//          (tempOauthCode, application) =>
//            val params = Params(tempOauthCode, application.id)
//
//            when(applicationRepo.find(params.applicationId)).thenReturn(
//              Future.successful(
//                Some(
//                  application
//                    .updateClientInfo(Some(ApplicationClientId("test")), None)
//                )
//              )
//            )
//
//            val result =
//              new InstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
//                .exec(params)
//
//            whenReady(result.failed) { e =>
//              assert(
//                e === SystemError(
//                  "error while get application client secret in install application use case"
//                )
//              )
//            }
//        }
//      }
//    }
//
//    "failed in workSpaceRepository.find" should {
//      "throw use case error and not invoked workSpaceRepository.update" in {
//        forAll(temporaryOauthCodeGen, applicationGen) {
//          (tempOauthCode, _application) =>
//            val application = _application.updateClientInfo(
//              Some(ApplicationClientId("test")),
//              Some(ApplicationClientSecret("test"))
//            )
//            val params      = Params(tempOauthCode, application.id)
//
//            when(applicationRepo.find(params.applicationId))
//              .thenReturn(Future.successful(Some(application)))
//            when(
//              workSpaceRepo.find(
//                params.temporaryOauthCode,
//                ApplicationClientId("test"),
//                ApplicationClientSecret("test")
//              )
//            ).thenReturn(Future.successful(None))
//
//            val result =
//              new InstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
//                .exec(params)
//
//            whenReady(result.failed) { e =>
//              assert(
//                e === NotFoundError(
//                  "error while workSpaceRepository.find in install application use case"
//                )
//              )
//            }
//        }
//      }
//    }
//
//    "failed in workSpaceRepository.update" should {
//      "throw use case error" in {
//        forAll(temporaryOauthCodeGen, applicationGen, workSpaceGen) {
//          (tempOauthCode, _application, workSpace) =>
//            val application = _application.updateClientInfo(
//              Some(ApplicationClientId("test")),
//              Some(ApplicationClientSecret("test"))
//            )
//            val params      = Params(tempOauthCode, application.id)
//
//            when(applicationRepo.find(params.applicationId))
//              .thenReturn(Future.successful(Some(application)))
//            when(
//              workSpaceRepo.find(
//                params.temporaryOauthCode,
//                ApplicationClientId("test"),
//                ApplicationClientSecret("test")
//              )
//            ).thenReturn(Future.successful(Some(workSpace)))
//            when(
//              workSpaceRepo.update(
//                workSpace.installApplication(application).unsafeGet,
//                application.id
//              )
//            ).thenReturn(Future.failed(DBError("error")))
//
//            val result =
//              new InstallApplicationUseCaseImpl(workSpaceRepo, applicationRepo)
//                .exec(params)
//
//            whenReady(result.failed) { e =>
//              assert(
//                e === SystemError(
//                  "error while workSpaceRepository.update in install application use case" + "\n"
//                    + DBError("error").getMessage
//                )
//              )
//            }
//        }
//      }
//    }
//  }
//}
