package infra.dao.slack

import com.google.inject.Inject
import infra.APIError
import infra.dao.ApiDao
import infra.dao.slack.UsersDaoImpl._
import play.api.libs.ws.WSClient
import io.circe.parser._
import infra.syntax.all._
import io.circe.generic.auto._
import io.circe._

import scala.concurrent.{ExecutionContext, Future}

trait UsersDao {
  def conversations(token: String): Future[ConversationResponse]
  def list(accessToken: String): Future[ListResponse]
}

class UsersDaoImpl @Inject() (ws: WSClient)(implicit ec: ExecutionContext)
    extends ApiDao(ws) with UsersDao {
  def conversations(accessToken: String): Future[ConversationResponse] = {
    val url = "https://slack.com/api/users.conversations"
    (for {
      res <- ws.url(url)
               .withQueryStringParameters("types" -> "public_channel,im")
               .withHttpHeaders("Authorization" -> s"Bearer $accessToken")
               .get()
               .ifFailedThenToInfraError(s"error while getting $url")
               .map(_.json.toString)
    } yield decode[ConversationResponse](res).left.map(e =>
      APIError(
        s"error while converting conversations api response -> token: $accessToken" + "\n" + e.getMessage + "\n" + res
      )
    )).anywaySuccess(ConversationResponse.empty)
  }

  def list(accessToken: String): Future[ListResponse] = {
    import infra.dao.slack.UsersDaoImpl._
    val url = "https://slack.com/api/users.list"
    (for {
      res <- ws.url(url)
               .withQueryStringParameters("pretty" -> "1")
               .withHttpHeaders("Authorization" -> s"Bearer $accessToken")
               .get()
               .ifFailedThenToInfraError(s"error while getting $url")
               .map(res => res.json.toString)
    } yield decode[ListResponse](res))
      .ifLeftThenToInfraError("error while converting list api response")
  }
}

object UsersDaoImpl {
  final case class ConversationResponse(channels: Seq[Channels])
  object ConversationResponse {
    def empty: ConversationResponse = ConversationResponse(Seq.empty)
  }

  final case class Channels(id: String)

  final case class ListResponse(members: Seq[Member])

  final case class Member(
    id: String,
    name: String,
    isBot: Boolean,
    deleted: Boolean,
    apiAppId: Option[String]
  )

  implicit val membersEncoder: Decoder[Member] = Decoder.instance { cursor =>
    for {
      id      <- cursor.downField("id").as[String]
      name    <- cursor.downField("name").as[String]
      isBot   <- cursor.downField("is_bot").as[Boolean]
      deleted <- cursor.downField("deleted").as[Boolean]
      botId   <-
        cursor.downField("profile").downField("api_app_id").as[Option[String]]
    } yield Member(id, name, isBot, deleted, botId)
  }
}
