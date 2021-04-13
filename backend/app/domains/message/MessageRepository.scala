package domains.message

import domains.message.Message.{MessageBlock, MessageChannelId}
import domains.workspace.WorkSpace.WorkSpaceToken

import scala.concurrent.Future

trait MessageRepository {
  def isEmpty(
    token: WorkSpaceToken,
    channelId: MessageChannelId
  ): Future[Boolean]

  def add(
    token: WorkSpaceToken,
    channelId: MessageChannelId,
    blocks: Seq[MessageBlock]
  ): Future[Unit]
}
