package infra.repositoryimpl

import com.google.inject.Inject
import domains.message.{Message, MessageRepository}
import domains.workspace.WorkSpace
import infra.dao.slack.{ConversationDao, ConversationDaoImpl}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class MessageRepositoryImpl @Inject() (
  protected val ws: WSClient,
  protected val conversationDao: ConversationDao
)(implicit val ec: ExecutionContext)
    extends MessageRepository {
  override def isEmpty(
    token: WorkSpace.WorkSpaceToken,
    channelId: Message.MessageChannelId
  ): Future[Boolean] = for {
    info <- conversationDao.info(token.value.value, channelId.value.value)
  } yield info.isFirst

  override def add(
    token: WorkSpace.WorkSpaceToken,
    channelId: Message.MessageChannelId,
    blocks: Seq[Message.MessageBlock]
  ): Future[Unit] = ???
}
