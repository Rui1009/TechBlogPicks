package infra.repositoryimpl

import com.google.inject.Inject
import domains.message.{Message, MessageRepository}
import domains.workspace.WorkSpace

import scala.concurrent.{ExecutionContext, Future}

class MessageRepositoryImpl @Inject() ()(implicit val ec: ExecutionContext)
    extends MessageRepository {
  override def isEmpty(
    token: WorkSpace.WorkSpaceToken,
    channelId: Message.MessageChannelId
  ): Future[Boolean] = ???

  override def add(
    token: WorkSpace.WorkSpaceToken,
    channelId: Message.MessageChannelId,
    blocks: Seq[Message.MessageBlock]
  ): Future[Unit] = ???
}
