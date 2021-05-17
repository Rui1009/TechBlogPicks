package usecases

import com.google.inject.Inject
import domains.channel.Channel.ChannelId
import domains.workspace.WorkSpace.WorkSpaceId
import domains.workspace.WorkSpaceRepository
import usecases.GreetInInvitedChannelUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait GreetInInvitedChannelUseCase {
  def exec(params: Params): Future[Unit]
}

object GreetInInvitedChannelUseCase {
  final case class Params(workSpaceId: WorkSpaceId, channelId: ChannelId)
}

final class GreetInInvitedChannelUseCaseImpl @Inject() (
  workSpaceRepository: WorkSpaceRepository
)(implicit val ec: ExecutionContext) extends GreetInInvitedChannelUseCase {
  override def exec(params: Params): Future[Unit] = for {
  targetWorkSpace <- workSpaceRepository.find(params.workSpaceId).ifNotExistsToUseCaseError(
    "error while workSpaceRepository.find in greet in invited channel use case"
  )
  } yield
}
