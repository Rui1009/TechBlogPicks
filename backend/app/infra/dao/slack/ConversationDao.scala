package infra.dao.slack

import com.google.inject.Inject
import infra.dao.ApiDao
import infra.dao.slack.ConversationDaoImpl.{InfoResponse, JoinResponse}
import infra.syntax.all._
import io.circe.Decoder._
import io.circe.parser._
import io.circe.{Decoder, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

trait ConversationDao {
  def info(token: String, channelId: String): Future[Option[InfoResponse]]
  def join(token: String, channelId: String): Future[JoinResponse]
}

class ConversationDaoImpl @Inject() (ws: WSClient)(implicit
  ec: ExecutionContext
) extends ApiDao(ws) with ConversationDao {
  def info(token: String, channelId: String): Future[Option[InfoResponse]] = {
    val url = "https://slack.com/api/conversations.info"
    (for {
      resp <- ws.url(url)
                .withHttpHeaders("Authorization" -> s"Bearer $token")
                .withQueryStringParameters("channel" -> channelId)
                .get()
                .ifFailedThenToInfraError(s"error while getting $url")
                .map(res => res.json.toString)
    } yield decode[Option[InfoResponse]](resp)).ifLeftThenToInfraError(
      "error while converting conversation info api response"
    )
  }

  def join(token: String, channelId: String): Future[JoinResponse] = {
    val url = "https://slack.com/api/conversations.join"
    (for {
      resp <- ws.url(url)
                .withHttpHeaders("Authorization" -> s"Bearer $token")
                .withQueryStringParameters("channel" -> channelId)
                .post(Json.Null.noSpaces)
                .ifFailedThenToInfraError(s"error while posting $url")
                .map(res => res.json.toString)
    } yield decode[JoinResponse](resp)).ifLeftThenToInfraError(
      "error while converting conversation join api response"
    )
  }
}

object ConversationDaoImpl {
  case class InfoResponse(senderUserId: String, text: String, ts: Float)
  implicit val conversationDecoder: Decoder[Option[InfoResponse]] =
    Decoder.instance { cursor =>
      if (cursor.downField("channel").downField("latest").failed) for {
        _ <-
          cursor
            .downField("channel")
            .downField("id")
            .as[String] // ??????????????????????????????????????????
      } yield None
      else cursor.downField("channel").downField("latest").as[Json] match {
        case Right(v) if v.isNull =>
          for {
            _ <-
              cursor
                .downField("channel")
                .downField("id")
                .as[String] // ??????????????????????????????????????????
          } yield None

        case Right(_) => for {
            senderUserId <- cursor
                              .downField("channel")
                              .downField("latest")
                              .downField("user")
                              .as[String]
            text         <- cursor
                              .downField("channel")
                              .downField("latest")
                              .downField("text")
                              .as[String]

            ts <- cursor
                    .downField("channel")
                    .downField("latest")
                    .downField("ts")
                    .as[String]
          } yield Some(InfoResponse(senderUserId, text, ts.toFloat))
      }
    }

  case class JoinResponse(channel: String)
  implicit
  val joinResponseDecoder: Decoder[JoinResponse] = Decoder.instance(cursor =>
    cursor
      .downField("channel")
      .downField("id")
      .as[String]
      .map(v => JoinResponse(v))
  )
}
