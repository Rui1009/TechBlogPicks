package usecases

import com.google.inject.Inject
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherTemporaryOauthCode
import domains.accesstokenpublisher.AccessTokenPublisherRepository
import domains.bot.{BotRepository}
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
    targetBot            <- botRepository
                              .find(params.botId)
                              .ifFailThenToUseCaseError(
                                "error while botRepository.find in install bot use case"
                              )
    accessTokenPublisher <-
      accessTokenPublisherRepository
        .find(params.temporaryOauthCode)
        .ifNotExistsToUseCaseError(
          "error while accessTokenPublisherRepository.find in install bot use case"
        )
    _                    <- botRepository
                              .update(
                                targetBot.receiveToken(accessTokenPublisher.publishToken),
                                accessTokenPublisher.publishToken
                              )
                              .ifFailThenToUseCaseError(
                                "error while botRepository.update in install bot use case"
                              )
  } yield ()
}