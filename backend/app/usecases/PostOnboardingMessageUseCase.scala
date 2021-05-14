package usecases

import com.google.inject.Inject
import domains.application.Application.ApplicationId
import domains.bot.Bot.BotId
import domains.channel.Channel.ChannelId
import domains.workspace.WorkSpace.WorkSpaceId
import domains.workspace.WorkSpaceRepository
import usecases.PostOnboardingMessageUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait PostOnboardingMessageUseCase {
  def exec(params: Params): Future[Unit]
}

object PostOnboardingMessageUseCase {
  final case class Params(
    applicationId: ApplicationId,
    workSpaceId: WorkSpaceId,
    channelId: ChannelId
  )
}

final class PostOnboardingMessageUseCaseImpl @Inject() (
  workSpaceRepository: WorkSpaceRepository
)(implicit val ec: ExecutionContext)
    extends PostOnboardingMessageUseCase {
  override def exec(params: Params) = (for {
    targetWorkSpace <-
      workSpaceRepository
        .find(params.workSpaceId)
        .ifNotExistsToUseCaseError(
          "error while workSpaceRepository.find in post onboarding message use case"
        )

    _              = println("use case exec")
    targetChannel <-
      targetWorkSpace
        .findChannel(params.channelId)
        .ifLeftThenToUseCaseError(
          "error while WorkSpace.findChannel in post onboarding message use case"
        )
    _              = println(targetChannel)
  } yield
    if (targetChannel.isMessageExists) {
      println("targetChannel message exists")
      Future.unit
    } else for {
      workSpaceWithUpdatedBots     <-
        targetWorkSpace
          .botCreateOnboardingMessage(params.applicationId)
          .ifLeftThenToUseCaseError(
            "error while WorkSpace.botCreateOnboardingMessage in post onboarding message use case"
          )
      workSpaceWithUpdatedChannels <-
        workSpaceWithUpdatedBots
          .botPostMessage(params.applicationId, targetChannel.id)
          .ifLeftThenToUseCaseError(
            "error while WorkSpace.botPostMessage in post onboarding message use case"
          )
      _                             = println("before send message")
      _                            <- workSpaceRepository
                                        .sendMessage(
                                          workSpaceWithUpdatedChannels,
                                          params.applicationId,
                                          params.channelId
                                        )
                                        .ifNotExistsToUseCaseError(
                                          "error while workSpaceRepository.sendMessage in post onboarding message use case"
                                        )
      _                             = println("after send message")
    } yield ()).flatten
}
