package usecases

import com.google.inject.Inject
import domains.application.Application.ApplicationId
import domains.application.ApplicationRepository
import domains.workspace.WorkSpace.WorkSpaceTemporaryOauthCode
import domains.workspace.WorkSpaceRepository
import usecases.InstallApplicationUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait InstallApplicationUseCase {
  def exec(params: Params): Future[Unit]
}

object InstallApplicationUseCase {
  final case class Params(
    temporaryOauthCode: WorkSpaceTemporaryOauthCode,
    applicationId: ApplicationId
  )
}

final class InstallApplicationUseCaseImpl @Inject() (
  workSpaceRepository: WorkSpaceRepository,
  applicationRepository: ApplicationRepository
)(implicit val ec: ExecutionContext)
    extends InstallApplicationUseCase {
  override def exec(params: Params): Future[Unit] = for {
    targetApplication <-
      applicationRepository
        .find(params.applicationId)
        .ifNotExistsToUseCaseError(
          "error while applicationRepository.find in install application use case"
        )

    targetApplicationClientId <-
      targetApplication.clientId.ifNotExistsToUseCaseError(
        "error while get application client id in install application use case"
      )

    targetApplicationClientSecret <-
      targetApplication.clientSecret.ifNotExistsToUseCaseError(
        "error while get application client secret in install application use case"
      )

    workSpace <-
      workSpaceRepository
        .find(
          params.temporaryOauthCode,
          targetApplicationClientId,
          targetApplicationClientSecret
        )
        .ifNotExistsToUseCaseError(
          "error while workSpaceRepository.find in install application use case"
        )

    updatedWorkSpace <-
      workSpace
        .installApplication(targetApplication) // ここで重複があるかどうかをみる。なければupdate
        .ifLeftThenToUseCaseError(
          "error while workSpace.installApplication in install application use case"
        )

    _ <- workSpaceRepository
           .update(updatedWorkSpace, targetApplication.id)
           .ifNotExistsToUseCaseError(
             "error while workSpaceRepository.update in install application use case"
           )
  } yield ()
}
