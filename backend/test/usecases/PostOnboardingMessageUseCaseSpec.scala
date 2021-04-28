//package usecases
//
//import domains.message.Message
//import domains.workspace.WorkSpaceRepository
//import helpers.traits.UseCaseSpec
//import usecases.PostOnboardingMessageUseCase.Params
//import eu.timepit.refined.auto._
//import infra.{APIError, DBError}
//
//import scala.concurrent.Future
//
//class PostOnboardingMessageUseCaseSpec extends UseCaseSpec {
//  "exec" when {
//    val workSpaceRepo = mock[WorkSpaceRepository]
//
//    "succeed when no channel messages exist" should {
//      "invoke workSpaceRepository.find & workSpaceRepository.sendMessage once" in {
//        forAll(workSpaceGen, botIdGen, botGen, channelTypedChannelMessageGen) {
//          (workSpace, botId, bot, channel) =>
//            val params            = Params(botId, workSpace.id, channel.id)
//            val returnedWorkSpace = workSpace.copy(
//              channels = Seq(channel.copy(id = channel.id, history = Seq())),
//              bots = Seq(bot.copy(id = Some(botId)))
//            )
//            val targetChannel     =
//              returnedWorkSpace.findChannel(params.channelId).unsafeGet
//
//            when(workSpaceRepo.find(params.workSpaceId))
//              .thenReturn(Future.successful(Some(returnedWorkSpace)))
//            val onboardingMessage = returnedWorkSpace
//              .botCreateOnboardingMessage(params.botId)
//              .unsafeGet
//            val targetBot         = returnedWorkSpace
//              .botPostMessage(params.botId, targetChannel.id, onboardingMessage)
//              .unsafeGet
//            when(
//              workSpaceRepo
//                .sendMessage(targetBot, targetChannel, onboardingMessage)
//            ).thenReturn(Future.successful(()))
//
//            new PostOnboardingMessageUseCaseImpl(workSpaceRepo)
//              .exec(params)
//              .futureValue
//
//            verify(workSpaceRepo).find(params.workSpaceId)
//            verify(workSpaceRepo).sendMessage(
//              targetBot,
//              targetChannel,
//              onboardingMessage
//            )
//
//            reset(workSpaceRepo)
//        }
//      }
//    }
//
//    "succeed when any channel message exists" should {
//      "invoke workSpaceRepository.find & workSpaceRepository.sendMessage never invokes" in {
//        forAll(workSpaceGen, botIdGen, botGen, channelTypedChannelMessageGen) {
//          (workSpace, botId, bot, channel) =>
//            val params            = Params(botId, workSpace.id, channel.id)
//            val returnedWorkSpace = workSpace.copy(
//              channels = Seq(channel.copy(id = channel.id)),
//              bots = Seq(bot.copy(id = Some(botId)))
//            )
//            val targetChannel     =
//              returnedWorkSpace.findChannel(params.channelId).unsafeGet
//
//            when(workSpaceRepo.find(params.workSpaceId))
//              .thenReturn(Future.successful(Some(returnedWorkSpace)))
//
//            new PostOnboardingMessageUseCaseImpl(workSpaceRepo)
//              .exec(params)
//              .futureValue
//
//            verify(workSpaceRepo).find(params.workSpaceId)
//            verify(workSpaceRepo, times(0)).sendMessage(*, *, *)
//
//            reset(workSpaceRepo)
//        }
//      }
//    }
//
//    "return None in workSpaceRepository.find" should {
//      "throw use case error & workSpaceRepository.sendMessage never invokes" in {
//        forAll(workSpaceGen, botIdGen, channelTypedChannelMessageGen) {
//          (workSpace, botId, channel) =>
//            val params = Params(botId, workSpace.id, channel.id)
//
//            when(workSpaceRepo.find(params.workSpaceId))
//              .thenReturn(Future.successful(None))
//
//            val result =
//              new PostOnboardingMessageUseCaseImpl(workSpaceRepo).exec(params)
//
//            whenReady(result.failed) { e =>
//              assert(
//                e === NotFoundError(
//                  "error while workSpaceRepository.find in post onboarding message use case"
//                )
//              )
//              verify(workSpaceRepo).find(params.workSpaceId)
//              verify(workSpaceRepo, times(0)).sendMessage(*, *, *)
//
//              reset(workSpaceRepo)
//            }
//        }
//      }
//    }
//
//    "failed in workSpaceRepository.sendMessage" should {
//      "throw use case error" in {
//        forAll(workSpaceGen, botIdGen, botGen, channelTypedChannelMessageGen) {
//          (workSpace, botId, bot, channel) =>
//            val params            = Params(botId, workSpace.id, channel.id)
//            val returnedWorkSpace = workSpace.copy(
//              channels = Seq(channel.copy(id = channel.id, history = Seq())),
//              bots = Seq(bot.copy(id = Some(botId)))
//            )
//            val targetChannel     =
//              returnedWorkSpace.findChannel(params.channelId).unsafeGet
//
//            when(workSpaceRepo.find(params.workSpaceId))
//              .thenReturn(Future.successful(Some(returnedWorkSpace)))
//            val onboardingMessage = returnedWorkSpace
//              .botCreateOnboardingMessage(params.botId)
//              .unsafeGet
//            val targetBot         = returnedWorkSpace
//              .botPostMessage(params.botId, targetChannel.id, onboardingMessage)
//              .unsafeGet
//            when(
//              workSpaceRepo
//                .sendMessage(targetBot, targetChannel, onboardingMessage)
//            ).thenReturn(Future.failed(APIError("error")))
//
//            val result =
//              new PostOnboardingMessageUseCaseImpl(workSpaceRepo).exec(params)
//
//            whenReady(result.failed) { e =>
//              assert(
//                e === SystemError(
//                  "error while workSpaceRepository.sendMessage in post onboarding use case" + "\n" +
//                    APIError("error").getMessage
//                )
//              )
//              verify(workSpaceRepo).find(params.workSpaceId)
//              verify(workSpaceRepo).sendMessage(
//                targetBot,
//                targetChannel,
//                onboardingMessage
//              )
//
//              reset(workSpaceRepo)
//            }
//        }
//      }
//    }
//
//    "failed in messageRepository.add" should {
//      "throw use case error" in {
//        forAll(workSpaceGen, botGen, messageGen) { (workSpace, bot, message) =>
//          val params =
//            Params(bot.id, workSpace.id, message.channelId, message.userId)
//
//          val targetToken = WorkSpaceToken("mockToken")
//          when(workSpaceRepo.find(params.workSpaceId, params.botId)).thenReturn(
//            Future.successful(Some(workSpace.copy(tokens = Seq(targetToken))))
//          )
//          when(messageRepo.isEmpty(targetToken, params.channelId))
//            .thenReturn(Future.successful(true))
//          when(
//            messageRepo.add(
//              targetToken,
//              params.channelId,
//              Message.onboardingMessage(params.userId, params.channelId).blocks
//            )
//          ).thenReturn(Future.failed(DBError("error")))
//
//          val result =
//            new PostOnboardingMessageUseCaseImpl(workSpaceRepo, messageRepo)
//              .exec(params)
//
//          whenReady(result.failed) { e =>
//            assert(
//              e === SystemError(
//                "error while messageRepository.add in post onboarding message use case" + "\n" + DBError(
//                  "error"
//                ).getMessage
//              )
//            )
//          }
//        }
//      }
//    }
//
//    "return false in messageRepository.isEmpty" should {
//      "return future unit without exec messageRepository.add" in {
//        forAll(workSpaceGen, botGen, messageGen) { (workSpace, bot, message) =>
//          val params =
//            Params(bot.id, workSpace.id, message.channelId, message.userId)
//
//          val targetToken = WorkSpaceToken("mockToken")
//          when(workSpaceRepo.find(params.workSpaceId, params.botId)).thenReturn(
//            Future.successful(Some(workSpace.copy(tokens = Seq(targetToken))))
//          )
//          when(messageRepo.isEmpty(targetToken, params.channelId))
//            .thenReturn(Future.successful(false))
//
//          val result: Unit = new PostOnboardingMessageUseCaseImpl(
//            workSpaceRepo,
//            messageRepo
//          ).exec(params).futureValue
//
//          verify(workSpaceRepo).find(params.workSpaceId, params.botId)
//          verify(messageRepo).isEmpty(targetToken, params.channelId)
//          verify(messageRepo, times(0)).add(
//            targetToken,
//            params.channelId,
//            Message.onboardingMessage(params.userId, params.channelId).blocks
//          )
//          assert(result === ())
//          reset(workSpaceRepo)
//          reset(messageRepo)
//        }
//      }
//    }
//  }
//}
