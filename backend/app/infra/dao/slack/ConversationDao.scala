package infra.dao.slack

import com.google.inject.Inject
import domains.workspace.WorkSpace.WorkSpaceToken
import infra.dao.ApiDao
import infra.dao.slack.ConversationDaoImpl.InfoResponse
import io.circe.{Decoder, Json}
import io.circe.Decoder.Result
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait ConversationDao {
  def info(token: String, channelId: String): Future[InfoResponse]
}

class ConversationDaoImpl @Inject() (ws: WSClient)(implicit
  ec: ExecutionContext
) extends ApiDao(ws) with ConversationDao {
  def info(token: String, channelId: String): Future[InfoResponse] = ???
}

object ConversationDaoImpl {
  case class InfoResponse(isFirst: Boolean)
  implicit
  val conversationDecoder: Decoder[InfoResponse] = Decoder.instance { cursor =>
    cursor
      .downField("channel")
      .downField("latest")
      .withFocus(lh =>
        if (lh.isNull) Json.obj("isFirst" -> Json.fromBoolean(true))
        else Json.obj("isFirst"           -> Json.fromBoolean(false))
      )
      .downField("isFirst")
      .as[Boolean]
      .map(isFirst => InfoResponse(isFirst))
  }
}
