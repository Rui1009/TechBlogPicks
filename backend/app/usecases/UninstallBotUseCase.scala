package usecases

import com.google.inject.Inject
import domains.bot.Bot.BotId
import domains.workspace.WorkSpace.{WorkSpaceId, WorkSpaceToken}
import domains.bot.BotRepository
import domains.workspace.WorkSpaceRepository
import usecases.UninstallBotUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait UninstallBotUseCase {
  def exec(params: Params): Future[Unit]
}

object UninstallBotUseCase {
  final case class Params(botId: BotId, workSpaceId: WorkSpaceId)
}

final class UninstallBotUseCaseImpl @Inject() (
  botRepository: BotRepository,
  workSpaceRepository: WorkSpaceRepository
)(implicit val ec: ExecutionContext)
    extends UninstallBotUseCase {
  object WorkSpaceNotFound extends Exception

  override def exec(params: Params): Future[Unit] = (for {
    targetBot       <- botRepository
                         .find(params.botId)
                         .ifFailThenToUseCaseError(
                           "error while botRepository.find in uninstall bot use case"
                         )
    targetWorkSpace <- workSpaceRepository.find(params.workSpaceId).map {
                         case Some(v) => v
                         case None    => throw WorkSpaceNotFound
                       }
    updatedWorkSpace = targetWorkSpace.uninstallBot(targetBot)
    _               <- workSpaceRepository
                         .add(updatedWorkSpace)
                         .ifFailThenToUseCaseError(
                           "error while workSpaceRepository.add in uninstall bot use case"
                         )
  } yield ()).recoverWith { case WorkSpaceNotFound => Future.unit }
}
