package usecases

import com.google.inject.Inject
import domains.bot.Bot._
import domains.bot.BotRepository
import usecases.UpdateBotClientInfoUseCase._

import scala.concurrent.{ExecutionContext, Future}

trait UpdateBotClientInfoUseCase {
  def exec(params: Params): Future[Unit]
}

object UpdateBotClientInfoUseCase {
  final case class Params(
    botId: BotId,
    botClientId: Option[BotClientId],
    botClientSecret: Option[BotClientSecret]
  )
}

final class UpdateBotClientInfoUseCaseImpl @Inject() (
  botRepository: BotRepository
)(implicit val ec: ExecutionContext)
    extends UpdateBotClientInfoUseCase {
  override def exec(params: Params): Future[Unit] = for {
    bot <-
      botRepository
        .find(params.botId)
        .ifNotExistsToUseCaseError(
          "error while botRepository.find in update bot client info use case"
        )

    updatedBot =
      bot.updateClientInfo(params.botClientId, params.botClientSecret)

    _ <-
      botRepository
        .update(updatedBot)
        .ifFailThenToUseCaseError(
          "error while botRepository.update in update bot client info use case"
        )
  } yield ()
}
