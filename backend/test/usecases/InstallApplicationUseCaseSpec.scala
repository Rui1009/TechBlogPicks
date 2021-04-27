package usecases

import domains.application.ApplicationRepository
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
          val application = _application
            .copy(clientId = Some(clientId), clientSecret = Some(clientSecret))
          val params      = Params(tempOauthCode, application.id)
//          val workSpace = _workSpace.copy(tokens = Seq(token), botIds = Seq())

          when(applicationRepo.find(params.applicationId))
            .thenReturn(Future.successful(Some(application)))
          when(
            workSpaceRepo.find(
              params.temporaryOauthCode,
              application.clientId.get,
              application.clientSecret.get
            )
          ).thenReturn(Future.successful(Some(_workSpace)))
          when(workSpaceRepo.update(_workSpace.installApplication(application)))
            .thenReturn(Future.unit)

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
            _workSpace.installApplication(application)
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
                  "error while botRepository.find in install bot use case"
                )
              )
            }
        }
      }
    }

    "returned clientId which is None" should {
      "throw use case error and not invoked workSpaceRepository.find and workSpaceRepository.update" in {
        forAll(temporaryOauthCodeGen, botGen) { (tempOauthCode, bot) =>
          val params = Params(tempOauthCode, bot.id)

          when(botRepo.find(params.botId)).thenReturn(
            Future.successful(
              Some(bot.updateClientInfo(None, Some(BotClientSecret("test"))))
            )
          )

          val result = new InstallApplicationUseCaseImpl(workSpaceRepo, botRepo)
            .exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === SystemError(
                "error while get bot client id in install bot use case"
              )
            )
          }
        }
      }
    }

    "returned clientSecret which is None" should {
      "throw use case error and not invoked workSpaceRepository.find and workSpaceRepository.update" in {
        forAll(temporaryOauthCodeGen, botGen) { (tempOauthCode, bot) =>
          val params = Params(tempOauthCode, bot.id)

          when(botRepo.find(params.botId)).thenReturn(
            Future.successful(
              Some(bot.updateClientInfo(Some(BotClientId("test")), None))
            )
          )

          val result = new InstallApplicationUseCaseImpl(workSpaceRepo, botRepo)
            .exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === SystemError(
                "error while get bot client secret in install bot use case"
              )
            )
          }
        }
      }
    }

    "failed in workSpaceRepository.find" should {
      "throw use case error and not invoked workSpaceRepository.update" in {
        forAll(
          temporaryOauthCodeGen,
          nonOptionBotGen,
          workSpaceGen,
          accessTokenGen
        ) { (tempOauthCode, bot, _workSpace, token) =>
          val params    = Params(tempOauthCode, bot.id)
          val workSpace = _workSpace.copy(tokens = Seq(token), botIds = Seq())

          when(botRepo.find(params.botId))
            .thenReturn(Future.successful(Some(bot)))
          when(
            workSpaceRepo.find(
              params.temporaryOauthCode,
              bot.clientId.get,
              bot.clientSecret.get
            )
          ).thenReturn(Future.successful(None))

          val result = new InstallApplicationUseCaseImpl(workSpaceRepo, botRepo)
            .exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === NotFoundError(
                "error while workSpaceRepository.find in install bot use case"
              )
            )
            verify(workSpaceRepo, never).update(workSpace.installBot(bot))
          }
        }
      }
    }

    "failed in workSpaceRepository.update" should {
      "throw use case error" in {
        forAll(
          temporaryOauthCodeGen,
          nonOptionBotGen,
          workSpaceGen,
          accessTokenGen
        ) { (tempOauthCode, bot, _workSpace, token) =>
          val params    = Params(tempOauthCode, bot.id)
          val workSpace = _workSpace.copy(tokens = Seq(token), botIds = Seq())

          when(botRepo.find(params.botId))
            .thenReturn(Future.successful(Some(bot)))
          when(
            workSpaceRepo.find(
              params.temporaryOauthCode,
              bot.clientId.get,
              bot.clientSecret.get
            )
          ).thenReturn(Future.successful(Some(workSpace)))
          when(workSpaceRepo.update(workSpace.installBot(bot)))
            .thenReturn(Future.failed(DBError("error")))

          val result = new InstallApplicationUseCaseImpl(workSpaceRepo, botRepo)
            .exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === SystemError(
                "error while workSpaceRepository.update in install bot use case" + "\n"
                  + DBError("error").getMessage
              )
            )
          }
        }
      }
    }
  }
}
