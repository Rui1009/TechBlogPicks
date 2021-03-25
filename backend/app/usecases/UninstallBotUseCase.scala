package usecases

import com.google.inject.Inject
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import domains.bot.BotRepository
import usecases.UninstallBotUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait UninstallBotUseCase {
  def exec(params: Params): Future[Unit]
}

object UninstallBotUseCase {
  final case class Params(accessToken: AccessTokenPublisherToken)
}

final class UninstallBotUseCaseImpl @Inject() (botRepository: BotRepository)(
  implicit val ec: ExecutionContext
) extends UninstallBotUseCase {
  override def exec(params: Params): Future[Unit] = for {
    _ <- botRepository
           .update(params.accessToken)
           .ifFailThenToUseCaseError(
             "error while botRepository.update in uninstall bot use case"
           )
  } yield ()
}
