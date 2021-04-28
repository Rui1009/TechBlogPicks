package usecases

import com.google.inject.Inject
import domains.application.Application._
import domains.application.ApplicationRepository
import usecases.UpdateApplicationClientInfoUseCase._

import scala.concurrent.{ExecutionContext, Future}

trait UpdateApplicationClientInfoUseCase {
  def exec(params: Params): Future[Unit]
}

object UpdateApplicationClientInfoUseCase {
  final case class Params(
    applicationId: ApplicationId,
    applicationClientId: Option[ApplicationClientId],
    applicationClientSecret: Option[ApplicationClientSecret]
  )
}

final class UpdateApplicationClientInfoUseCaseImpl @Inject() (
  applicationRepository: ApplicationRepository
)(implicit val ec: ExecutionContext)
    extends UpdateApplicationClientInfoUseCase {
  override def exec(params: Params): Future[Unit] = for {
    application <-
      applicationRepository
        .find(params.applicationId)
        .ifNotExistsToUseCaseError(
          "error while applicationRepository.find in update bot client info use case"
        )

    updatedApplication = application.updateClientInfo(
                           params.applicationClientId,
                           params.applicationClientSecret
                         )

    _ <- applicationRepository
           .update(updatedApplication)
           .ifFailThenToUseCaseError(
             "error while applicationRepository.update in update bot client info use case"
           )
  } yield ()
}
