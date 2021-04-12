package usecases

import com.google.inject.Inject
import domains.bot.Bot.BotId
import domains.message.Message.MessageChannelId
import domains.message.MessageRepository
import domains.workspace.WorkSpace.WorkSpaceId
import domains.workspace.{WorkSpace, WorkSpaceRepository}
import usecases.PostOnboardingMessageUseCase.Params

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

trait PostOnboardingMessageUseCase {
  def exec(params: Params): Future[Unit]
}

object PostOnboardingMessageUseCase {
  final case class Params(
    botId: BotId,
    workSpaceId: WorkSpaceId,
    channelId: MessageChannelId
  )
}

final class PostOnboardingMessageUseCaseImpl @Inject() (
  workSpaceRepository: WorkSpaceRepository,
  messageRepository: MessageRepository
)(implicit val ec: ExecutionContext)
    extends PostOnboardingMessageUseCase {
  object MessageIsNotEmpty extends Exception
  override def exec(params: Params): Future[Unit] = (for {
    targetWorkSpace <-
      workSpaceRepository
        .find(params.workSpaceId, params.botId)
        .ifNotExistsToUseCaseError(
          "error while workSpaceRepository.find in post onboarding message use case"
        )
    targetToken      = targetWorkSpace.tokens.head
    _               <- messageRepository
                         .isEmpty(targetToken, params.channelId)
                         .ifFailThenToUseCaseError(
                           "error while messageRepository.isEmpty in post onboarding message use case"
                         )
                         .map {
                           case true  => true
                           case false => throw MessageIsNotEmpty
                         }
    _               <- messageRepository
                         .add(
                           targetToken,
                           params.channelId,
                           Seq()
                         ) // どこでblocks(投稿される内容)を定義するかは決めてないのでとりあえず空配列で
                         .ifFailThenToUseCaseError(
                           "error while messageRepository.add in post onboarding message use case"
                         )
  } yield ()).recoverWith { case MessageIsNotEmpty => Future.unit }
}
