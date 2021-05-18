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
  object DuplicateBot extends Exception
  override def exec(params: Params): Future[Unit] = (for {
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
        .ifFailThenToUseCaseError(
          "error while workSpaceRepository.find in install application use case"
        )

    updatedWorkSpace <- workSpace.installApplication(targetApplication) match {
                          case Right(v) => Future.successful(v)
                          case Left(_)  => throw DuplicateBot
                        } // ここで重複があるかどうかをみる。なければupdate。エラーの型でbot duplicateの時のみthrowすべき。そもそもthrowでいいのか

    _                <- workSpaceRepository
                          .update(updatedWorkSpace, targetApplication.id)
                          .ifNotExistsToUseCaseError(
                            "error while workSpaceRepository.update in install application use case"
                          )
  } yield ()).recoverWith { case DuplicateBot => Future.unit }
}
