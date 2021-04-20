package usecases

import com.google.inject.Inject
import domains.bot.Bot.{BotChannelId, BotId}
import domains.bot.{Bot, BotRepository}
import domains.workspace.WorkSpace.WorkSpaceId
import usecases.JoinChannelUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait JoinChannelUseCase {
  def exec(params: Params): Future[Unit]
}

object JoinChannelUseCase {
  final case class Params(channelId: BotChannelId, botId: BotId)
}

final class JoinChannelUseCaseImpl @Inject() (botRepository: BotRepository)(
  implicit val ec: ExecutionContext
) extends JoinChannelUseCase {
  override def exec(params: Params): Future[Unit] = for {
    targetBot <- botRepository
                   .find(params.botId)
                   .ifNotExistsToUseCaseError(
                     "error while botRepository.find in join channel use case"
                   )
    _         <- botRepository
                   .join(targetBot.joinedBot(params.channelId))
                   .ifFailThenToUseCaseError(
                     "error while botRepository.join in join channel use case"
                   )
  } yield ()
}
