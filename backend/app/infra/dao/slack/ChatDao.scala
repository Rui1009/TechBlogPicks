package infra.dao.slack

import com.google.inject.Inject
import infra.APIError
import infra.dao.slack.ChatDaoImpl.PostMessageResponse
import infra.syntax.all._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser._
import play.api.Logger
import play.api.http.ContentTypes.JSON
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait ChatDao {
  def postMessage(
    token: String,
    channel: String,
    text: String
  ): Future[PostMessageResponse]
}

class ChatDaoImpl @Inject() (ws: WSClient)(implicit ec: ExecutionContext)
    extends ChatDao {
  private val logger = Logger(this.getClass)

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
      .map(res =>
        decode[PostMessageResponse](res.json.toString).left.map(e =>
          APIError(
            "post message failed" + "\n" + e.getMessage + "\n" + res.json.toString
          )
        )
      )
      .transformWith {
        case Success(Right(v)) => Future.successful(v)
        case Success(Left(e))  => notifyError(e.getMessage.trim)
            .flatMap(_ => Future.successful(PostMessageResponse.empty))
        case Failure(e)        => notifyError(e.getMessage.trim)
            .flatMap(_ => Future.successful(PostMessageResponse.empty))
      }
  }

  private def notifyError(error: String) = {
    logger.error(error)
    (for {
      url <- sys.env.get("ERROR_NOTIFICATION_URL")
    } yield for {
      _ <- ws.url(url).post(Json.obj("text" -> Json.fromString(error)).noSpaces)
    } yield ()) match {
      case Some(v) => v
      case None    =>
        logger.warn("can not specify ERROR_NOTIFICATION_URL")
        Future.unit
    }
  }
}

object ChatDaoImpl {
  case class PostMessageResponse(channel: String)
  object PostMessageResponse {
    def empty: PostMessageResponse = PostMessageResponse("")
  }
}
