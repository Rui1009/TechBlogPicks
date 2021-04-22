package usecases

import com.google.inject.Inject
import domains.application.Application.{
  ApplicationClientId,
  ApplicationClientSecret,
  ApplicationId
}
import domains.application.ApplicationRepository
import usecases.UpdateBotClientInfoUseCase._

import scala.concurrent.{ExecutionContext, Future}

trait UpdateBotClientInfoUseCase {
  def exec(params: Params): Future[Unit]
}

object UpdateBotClientInfoUseCase {
  final case class Params(
    applicationId: ApplicationId,
    applicationClientId: Option[ApplicationClientId],
    applicationClientSecret: Option[ApplicationClientSecret]
  )
}

final class UpdateBotClientInfoUseCaseImpl @Inject() (
  applicationRepository: ApplicationRepository
)(implicit val ec: ExecutionContext)
    extends UpdateBotClientInfoUseCase {
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
