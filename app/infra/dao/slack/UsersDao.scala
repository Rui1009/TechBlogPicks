package infra.dao.slack

import com.google.inject.Inject
import infra.dao.slack.UsersDaoImpl._
import play.api.libs.ws.WSClient
import io.circe.parser._
import io.circe.generic.auto._
import infra.syntax.all._
import io.circe._

import scala.concurrent.{ExecutionContext, Future}

trait UsersDao {
  def conversations(token: String, id: String): Future[ConversationResponse]
  def list(accessToken: String): Future[ListResponse]
}

class UsersDaoImpl @Inject()(ws: WSClient)(implicit val ec: ExecutionContext)
    extends UsersDao {
  def conversations(
      accessToken: String,
      id: String
  ): Future[ConversationResponse] = {
    val url = "https://slack.com/api/users.conversations"
    (for {
      res <- ws
        .url(url)
        .withQueryStringParameters("user" -> id)
        .withHttpHeaders("Authorization" -> s"Bearer $accessToken")
        .get
        .ifFailedThenToInfraError(s"error while getting $url")
        .map(_.json.toString)
    } yield decode[ConversationResponse](res)).ifLeftThenToInfraError(
      "error while converting conversation api response"
    )
  }

  def list(accessToken: String): Future[ListResponse] = {
    val url = "https://slack.com/api/users.list?pretty=1"
    (for {
      res <- ws
        .url(url)
        .withQueryStringParameters("pretty" -> "1")
        .withHttpHeaders("Authorization" -> s"Bearer $accessToken")
        .get
        .ifFailedThenToInfraError(s"error while getting $url")
        .map(_.json.toString)
    } yield decode[ListResponse](res)).ifLeftThenToInfraError(
      "error while converting list api response"
    )
  }
}

object UsersDaoImpl {
  case class ConversationResponse(channels: Seq[Channels])
  case class Channels(id: String)

  case class ListResponse(members: Seq[Members])
  case class Members(id: String, realName: String, isBot: Boolean)
  implicit val membersEncoder: Encoder[Members] =
    Encoder.forProduct3("id", "real_name", "is_bot")(m =>
      (m.id, m.realName, m.isBot))
}
