package usecases

import com.google.inject.Inject
import domains.application.Application.ApplicationId
import domains.channel.Channel.ChannelId
import domains.workspace.WorkSpace.WorkSpaceId
import domains.workspace.WorkSpaceRepository
import usecases.JoinChannelUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait JoinChannelUseCase {
  def exec(params: Params): Future[Unit]
}

object JoinChannelUseCase {
  final case class Params(
    channelId: ChannelId,
    applicationId: ApplicationId,
    workSpaceId: WorkSpaceId
  )
}

final class JoinChannelUseCaseImpl @Inject() (
  workSpaceRepository: WorkSpaceRepository
)(implicit val ec: ExecutionContext)
    extends JoinChannelUseCase {
  override def exec(params: Params) = for {
    workSpace <-
      workSpaceRepository
        .find(params.workSpaceId)
        .ifNotExistsToUseCaseError(
          "error while workSpaceRepository.find in join channel use case"
        )

    updatedWorkSpace <-
      workSpace
        .addBotToChannel(params.applicationId, params.channelId)
        .ifLeftThenToUseCaseError(
          "error while WorkSpace.addBotToChannel in join channel use case"
        )

    _ <- workSpaceRepository
           .joinChannels(
             updatedWorkSpace,
             params.applicationId,
             Seq(params.channelId)
           )
           .ifFailThenToUseCaseError(
             "error while WorkSpaceRepository.join in join channel use case"
           )
  } yield ()
}
