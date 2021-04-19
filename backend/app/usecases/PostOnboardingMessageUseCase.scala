package usecases

import com.google.inject.Inject
import domains.bot.Bot.BotId
import domains.message.Message.{
  AccessoryImage,
  ActionBlock,
  ActionSelect,
  BlockText,
  MessageChannelId,
  SectionBlock,
  SelectPlaceHolder
}
import domains.message.MessageRepository
import domains.workspace.WorkSpace.WorkSpaceId
import domains.workspace.{WorkSpace, WorkSpaceRepository}
import eu.timepit.refined.api.Refined
import usecases.PostOnboardingMessageUseCase.Params
import eu.timepit.refined.auto._

import scala.concurrent.{ExecutionContext, Future}

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
  val imageUrl                                    =
    "https://img-mdpr.freetls.fastly.net/article/GEdz/wm/GEdzUf8cy7lGyXuQkG1UVx9LfKGYrBU76_uXSqbj86s.jpg?width=700&disable=upscale&auto=webp"
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
        Seq(
          SectionBlock(
            BlockText("test"),
            Some(AccessoryImage(Refined.unsafeApply(imageUrl), "text"))
          ),
          ActionBlock(
            Seq(
              ActionSelect(
                "channels_select",
                SelectPlaceHolder("Select a channel", false),
                "actionId-0"
              ),
              ActionSelect(
                "users_select",
                SelectPlaceHolder("Select a user", true),
                "testId-0"
              )
            )
          )
        )
      ) // どこでblocks(投稿される内容)を定義するかは決めてないのでとりあえず空配列で
      .ifFailThenToUseCaseError(
        "error while messageRepository.add in post onboarding message use case"
      )
    else Future.unit).flatten
}
