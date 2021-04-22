package usecases

import com.google.inject.Inject
import domains.application.Application.ApplicationId
import domains.application.ApplicationRepository
import domains.bot.Bot.BotId
import domains.workspace.WorkSpace.WorkSpaceId
import domains.bot.BotRepository
import domains.workspace.WorkSpaceRepository
import usecases.UninstallBotUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait UninstallBotUseCase {
  def exec(params: Params): Future[Unit]
}

object UninstallBotUseCase {
  final case class Params(
    workSpaceId: WorkSpaceId,
    applicationId: ApplicationId
  )
}

final class UninstallBotUseCaseImpl @Inject() (
  workSpaceRepository: WorkSpaceRepository,
  applicationRepository: ApplicationRepository
)(implicit val ec: ExecutionContext)
    extends UninstallBotUseCase {
  object WorkSpaceNotFound extends Exception

  override def exec(params: Params): Future[Unit] = (for {
    targetApplication <-
      applicationRepository
        .find(params.applicationId)
        .ifNotExistsToUseCaseError(
          "error while applicationRepository.find in uninstall bot use case"
        )
    targetWorkSpace   <- workSpaceRepository.find(params.workSpaceId).map {
                           case Some(v) => v
                           case None    => throw WorkSpaceNotFound
                         }
    updatedWorkSpace   = targetWorkSpace.uninstallApplication(targetApplication)
    _                 <- workSpaceRepository
                           .update(updatedWorkSpace)
                           .ifFailThenToUseCaseError(
                             "error while workSpaceRepository.add in uninstall bot use case"
                           )
  } yield ()).recoverWith { case WorkSpaceNotFound => Future.unit }
}
