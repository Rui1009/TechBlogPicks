package usecases

import com.google.inject.Inject
import domains.application.Application.ApplicationId
import domains.application.ApplicationRepository
import domains.workspace.WorkSpace.WorkSpaceTemporaryOauthCode
import domains.workspace.WorkSpaceRepository
import domains.bot.{Bot, BotRepository}
import domains.bot.Bot.BotId
import usecases.InstallBotUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait InstallBotUseCase {
  def exec(params: Params): Future[Unit]
}

object InstallBotUseCase {
  final case class Params(
    temporaryOauthCode: WorkSpaceTemporaryOauthCode,
    applicationId: ApplicationId
  )
}

final class InstallBotUseCaseImpl @Inject() (
  workSpaceRepository: WorkSpaceRepository,
  applicationRepository: ApplicationRepository
)(implicit val ec: ExecutionContext)
    extends InstallBotUseCase {
  override def exec(params: Params): Future[Unit] = for {
    targetApplication <-
      applicationRepository
        .find(params.applicationId)
        .ifNotExistsToUseCaseError(
          "error while applicationRepository.find in install bot use case"
        )

    targetApplicationClientId <-
      targetApplication.clientId.ifNotExistsToUseCaseError(
        "error while get application client id in install bot use case"
      )

    targetApplicationClientSecret <-
      targetApplication.clientSecret.ifNotExistsToUseCaseError(
        "error while get application client secret in install bot use case"
      )

    workSpace <-
      workSpaceRepository
        .find(
          params.temporaryOauthCode,
          targetApplicationClientId,
          targetApplicationClientSecret
        )
        .ifNotExistsToUseCaseError(
          "error while workSpaceRepository.find in install bot use case"
        )

    updatedWorkSpace = workSpace.installApplication(targetApplication)

    _ <- workSpaceRepository
           .add(updatedWorkSpace)
           .ifFailThenToUseCaseError(
             "error while workSpaceRepository.update in install bot use case"
           )
  } yield ()
}
