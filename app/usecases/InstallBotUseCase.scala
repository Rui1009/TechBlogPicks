package usecases

import com.google.inject.Inject
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherTemporaryOauthCode
import domains.accesstokenpublisher.AccessTokenPublisherRepository
import domains.bot.{Bot, BotRepository}
import domains.bot.Bot.BotId
import usecases.InstallBotUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait InstallBotUseCase {
  def exec(params: Params): Future[Unit]
}

object InstallBotUseCase {
  final case class Params(
    temporaryOauthCode: AccessTokenPublisherTemporaryOauthCode,
    botId: BotId
  )
}

final class InstallBotUseCaseImpl @Inject() (
  accessTokenPublisherRepository: AccessTokenPublisherRepository,
  botRepository: BotRepository
)(implicit val ec: ExecutionContext)
    extends InstallBotUseCase {
  override def exec(params: Params): Future[Unit] = for {
    accessTokenPublisher <-
      accessTokenPublisherRepository
        .find(params.temporaryOauthCode)
        .ifFailThenToUseCaseError(
          "error while accessTokenPublisher.find in install bot use case"
        )
    _                    <- botRepository.update(params.botId, accessTokenPublisher.token)
  } yield Unit
}
