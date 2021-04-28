package usecases

import com.google.inject.Inject
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
    botId: BotId,
    workSpaceId: WorkSpaceId,
    channelId: ChannelId
  )
}

final class PostOnboardingMessageUseCaseImpl @Inject() (
  workSpaceRepository: WorkSpaceRepository
)(implicit val ec: ExecutionContext)
    extends PostOnboardingMessageUseCase {
  override def exec(params: Params): Future[Unit] =
    for {
      targetWorkSpace <-
        workSpaceRepository
          .find(params.workSpaceId)
          .ifNotExistsToUseCaseError(
            "error while workSpaceRepository.find in post onboarding message use case"
          )

      targetChannel <-
        targetWorkSpace
          .findChannel(params.channelId)
          .ifLeftThenToUseCaseError(
            "error while WorkSpace.findChannel in post onboarding message use case"
          )
    } yield
      if (targetChannel.isMessageExists) Future.unit
      else
        for {
          onboardingMessage <-
            targetWorkSpace
              .botCreateOnboardingMessage(params.botId)
              .ifLeftThenToUseCaseError(
                "error while WorkSpace.botCreateOnboardingMessage in post onboarding message use case"
              )
          bot               <-
            targetWorkSpace
              .botPostMessage(params.botId, targetChannel.id, onboardingMessage)
              .ifLeftThenToUseCaseError(
                "error while WorkSpace.botPostMessage in post onboarding message use case"
              )
        } yield for {
          _ <- workSpaceRepository
                 .sendMessage(bot, targetChannel, onboardingMessage)
                 .ifFailThenToUseCaseError(
                   "error while workSpaceRepository.sendMessage in post onboarding use case"
                 )
        } yield Future.unit

}
