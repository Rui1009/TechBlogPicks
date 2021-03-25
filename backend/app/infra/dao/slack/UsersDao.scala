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
  def info(token: String, id: String): Future[InfoResponse]
}

class UsersDaoImpl @Inject() (ws: WSClient)(implicit ec: ExecutionContext)
    extends ApiDao(ws) with UsersDao {
  def conversations(accessToken: String): Future[ConversationResponse] = {
    val url = "https://slack.com/api/users.conversations"
    (for {
      res <- ws.url(url)
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

  def info(token: String, id: String): Future[InfoResponse] = {
    val url = "https://slack.com/api/users.info"

    (for {
      resp <- ws.url(url)
                .withHttpHeaders("Authorization" -> s"Bearer $token")
                .withQueryStringParameters("user" -> id)
                .post(Json.Null.noSpaces)
                .ifFailedThenToInfraError(s"error while getting $url")
                .map(res => res.json.toString)
    } yield decode[InfoResponse](resp))
      .ifLeftThenToInfraError("error while converting info api response")
  }
}

object UsersDaoImpl {
  case class ConversationResponse(channels: Seq[Channels])
  object ConversationResponse {
    def empty: ConversationResponse = ConversationResponse(Seq.empty)
  }
  case class Channels(id: String)

  case class ListResponse(members: Seq[Member])

  case class Member(id: String, name: String, isBot: Boolean, deleted: Boolean)
  implicit val membersEncoder: Decoder[Member] =
    Decoder.forProduct4("id", "name", "is_bot", "deleted")(
      (id, realName, isBot, deleted) => Member(id, realName, isBot, deleted)
    )

  case class InfoResponse(name: String)
  implicit
  val decodeBotName: Decoder[InfoResponse] = Decoder.instance { cursor =>
    for {
      botName <- cursor.downField("user").downField("name").as[String]
    } yield InfoResponse(botName)
  }
}
