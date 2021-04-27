package usecases

import domains.bot.Bot.{BotClientId, BotClientSecret}
import domains.bot.BotRepository
import domains.workspace.WorkSpaceRepository
import eu.timepit.refined.auto._
import helpers.traits.UseCaseSpec
import infra.DBError
import usecases.InstallApplicationUseCase._

import scala.concurrent.Future

class InstallApplicationUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val workSpaceRepo = mock[WorkSpaceRepository]
    val botRepo       = mock[BotRepository]
    "succeed" should {
      "invoke botRepository.find once & workSpaceRepository.find once & workSpaceRepository.update once" in {
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
            .thenReturn(Future.unit)

          new InstallApplicationUseCaseImpl(workSpaceRepo, botRepo)
            .exec(params)
            .futureValue

          verify(botRepo).find(params.botId)
          verify(workSpaceRepo).find(
            params.temporaryOauthCode,
            bot.clientId.get,
            bot.clientSecret.get
          )
          verify(workSpaceRepo).update(workSpace.installBot(bot))
          reset(workSpaceRepo)
          reset(botRepo)
        }
      }
    }

    "return None in botRepository.find" should {
      "throw use case error and not invoked workSpaceRepository.find & workSpaceRepository.update" in {
        forAll(
          temporaryOauthCodeGen,
          nonOptionBotGen,
          workSpaceGen,
          accessTokenGen
        ) { (tempOauthCode, bot, _workSpace, token) =>
          val params    = Params(tempOauthCode, bot.id)
          val workSpace = _workSpace.copy(tokens = Seq(token), botIds = Seq())

          when(botRepo.find(params.botId)).thenReturn(Future.successful(None))

          val result = new InstallApplicationUseCaseImpl(workSpaceRepo, botRepo)
            .exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === NotFoundError(
                "error while botRepository.find in install bot use case"
              )
            )
            verify(workSpaceRepo, never).find(
              params.temporaryOauthCode,
              bot.clientId.get,
              bot.clientSecret.get
            )
            verify(workSpaceRepo, never).update(workSpace.installBot(bot))
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
