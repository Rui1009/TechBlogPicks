package usecases

import com.google.inject.Inject
import domains.application.Application.ApplicationId
import domains.channel.Channel.ChannelId
import domains.workspace.WorkSpace.WorkSpaceId
import domains.workspace.WorkSpaceRepository
import usecases.GreetInInvitedChannelUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait GreetInInvitedChannelUseCase {
  def exec(params: Params): Future[Unit]
}

object GreetInInvitedChannelUseCase {
  final case class Params(
    workSpaceId: WorkSpaceId,
    channelId: ChannelId,
    applicationId: ApplicationId
  )
}

final class GreetInInvitedChannelUseCaseImpl @Inject() (
  workSpaceRepository: WorkSpaceRepository
)(implicit val ec: ExecutionContext)
    extends GreetInInvitedChannelUseCase {
  override def exec(params: Params): Future[Unit] = for {
    targetWorkSpace         <-
      workSpaceRepository
        .find(params.workSpaceId)
        .ifNotExistsToUseCaseError(
          "error while workSpaceRepository.find in greet in invited channel use case"
        )
    workSpaceWithUpdatedBot <-
      targetWorkSpace
        .botCreateGreetingInInvitedChannel(params.applicationId)
        .ifLeftThenToUseCaseError(
          "error while WorkSpace.botCreateGreetingInInvitedChannel in greet in invited channel use case"
        )

    workSpaceWithUpdatedChannel <-
      workSpaceWithUpdatedBot
        .botPostMessage(params.applicationId, params.channelId)
        .ifLeftThenToUseCaseError(
          "error while WorkSpace.botPostMessage in greet in invited channel use case"
        )

    _ <- workSpaceRepository
           .sendMessage(
             workSpaceWithUpdatedChannel,
             params.applicationId,
             params.channelId
           )
           .ifNotExistsToUseCaseError(
             "error while workSpaceRepository.sendMessage in greet in invited channel use case"
           )
  } yield ()
}
