package infra.repositoryimpl

import com.google.inject.Inject
import domains.message.Message.MessageBlock
import domains.message.{Message, MessageRepository}
import domains.workspace.WorkSpace
import infra.dao.slack.{ChatDao, ConversationDao, ConversationDaoImpl}
import io.circe._
import io.circe.syntax._
import play.api.libs.ws.WSClient
import io.circe.generic.semiauto._

import scala.concurrent.{ExecutionContext, Future}

class MessageRepositoryImpl @Inject() (
  protected val ws: WSClient,
  protected val conversationDao: ConversationDao,
  protected val chatDao: ChatDao
)(implicit val ec: ExecutionContext)
    extends MessageRepository {
  override def isEmpty(
    token: WorkSpace.WorkSpaceToken,
    channelId: Message.MessageChannelId
  ): Future[Boolean] = conversationDao
    .info(token.value.value, channelId.value.value)
    .map(_.isFirst)

  override def add(
    token: WorkSpace.WorkSpaceToken,
    channelId: Message.MessageChannelId,
    blocks: Seq[MessageBlock]
  ): Future[Unit] = for {
    _ <- chatDao.postMessage(token.value.value, channelId.value.value, blocks)
  } yield ()
}
