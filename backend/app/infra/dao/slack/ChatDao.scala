package infra.dao.slack

import com.google.inject.Inject
import infra.dao.slack.ChatDaoImpl.PostMessageResponse
import infra.syntax.all._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser._
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait ChatDao {
  def postMessage(
    token: String,
    channel: String,
    text: String
  ): Future[PostMessageResponse]
}

class ChatDaoImpl @Inject() (ws: WSClient)(implicit ec: ExecutionContext)
    extends ChatDao {
  def postMessage(
    token: String,
    channel: String,
    text: String
  ): Future[PostMessageResponse] = {
    val url = "https://slack.com/api/chat.postMessage"
    ws.url(url)
      .withHttpHeaders("Authorization" -> s"Bearer $token")
      .withQueryStringParameters("channel" -> channel, "text" -> text)
      .post(Json.Null.noSpaces)
      .ifFailedThenToInfraError(s"error while posting $url")
      .map(res => decode[PostMessageResponse](res.json.toString))
      .ifLeftThenToInfraError("post message failed")
  }
}

object ChatDaoImpl {
  case class PostMessageResponse(channel: String)
}
