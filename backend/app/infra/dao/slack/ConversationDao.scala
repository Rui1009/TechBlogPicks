package infra.dao.slack

import com.google.inject.Inject
import domains.workspace.WorkSpace.WorkSpaceToken
import infra.dao.ApiDao
import infra.dao.slack.ConversationDaoImpl.InfoResponse
import io.circe.{Decoder, Json}
import io.circe.Decoder.Result
import play.api.libs.ws.WSClient
import infra.syntax.all._
import io.circe.parser._
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

trait ConversationDao {
  def info(token: String, channelId: String): Future[InfoResponse]
}

class ConversationDaoImpl @Inject() (ws: WSClient)(implicit
  ec: ExecutionContext
) extends ApiDao(ws) with ConversationDao {
  private lazy val logger = Logger(this.getClass)
  def info(token: String, channelId: String): Future[InfoResponse] = {
    val url = "https://slack.com/api/conversations.info"
    (for {
      resp <- ws.url(url)
                .withHttpHeaders("Authorization" -> s"Bearer $token")
                .withQueryStringParameters("channel" -> channelId)
                .get()
                .ifFailedThenToInfraError(s"error while getting $url")
                .map(res => res.json.toString)
      _     = logger.warn(token)
      _     = logger.warn(channelId)
      _     = logger.warn(resp)
    } yield decode[InfoResponse](resp)).ifLeftThenToInfraError(
      "error while converting conversation info api response"
    )
  }
}

object ConversationDaoImpl {
  case class InfoResponse(latest: Json) {
    def isFirst: Boolean = this.latest.isNull
  }
  implicit val conversationDecoder: Decoder[InfoResponse] =
    Decoder.instance { cursor =>
      cursor
        .downField("channel")
        .downField("latest")
        .as[Json]
        .map(v => InfoResponse(v))
    }
}
