package usecases

import com.google.inject.Inject
import domains.bot.Bot.BotId
import domains.message.Message.{MessageChannelId, MessageUserId}
import domains.message.{Message, MessageRepository}
import domains.workspace.WorkSpace.WorkSpaceId
import domains.workspace.{WorkSpace, WorkSpaceRepository}
import usecases.PostOnboardingMessageUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait PostOnboardingMessageUseCase {
  def exec(params: Params): Future[Unit]
}

object PostOnboardingMessageUseCase {
  final case class Params(
    botId: BotId,
    workSpaceId: WorkSpaceId,
    channelId: MessageChannelId,
    userId: MessageUserId
  )
}

final class PostOnboardingMessageUseCaseImpl @Inject() (
  workSpaceRepository: WorkSpaceRepository,
  messageRepository: MessageRepository
)(implicit val ec: ExecutionContext)
    extends PostOnboardingMessageUseCase {
  override def exec(params: Params): Future[Unit] = (for {
    targetWorkSpace <-
      workSpaceRepository
        .find(params.workSpaceId, params.botId)
        .ifNotExistsToUseCaseError(
          "error while workSpaceRepository.find in post onboarding message use case"
        )
    targetToken      = targetWorkSpace.tokens.head
    isEmpty         <-
      messageRepository
        .isEmpty(targetToken, params.channelId)
        .ifFailThenToUseCaseError(
          "error while messageRepository.isEmpty in post onboarding message use case"
        )
  } yield
    if (isEmpty) messageRepository
      .add(
        targetToken,
        params.channelId,
        Message.onboardingMessage(params.userId, params.channelId).blocks
      )
      .ifFailThenToUseCaseError(
        "error while messageRepository.add in post onboarding message use case"
      )
    else Future.unit).flatten
}
