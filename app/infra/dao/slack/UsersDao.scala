package infra.dao.slack

import com.google.inject.Inject
import infra.dao.slack.UsersDaoImpl.ConversationResponse
import play.api.libs.ws.WSClient
import io.circe.parser._
import io.circe.generic.auto._
import infra.syntax.all._

import scala.concurrent.{ExecutionContext, Future}

trait UsersDao {
  def conversations(token: String, id: String): Future[ConversationResponse]
}

class UsersDaoImpl @Inject() (ws: WSClient)(implicit val ec: ExecutionContext)
    extends UsersDao {
  def conversations(
    accessToken: String,
    id: String
  ): Future[ConversationResponse] = {
    val url = "https://slack.com/api/users.conversations"
    (for {
      res <- ws.url(url)
               .withQueryStringParameters("user" -> id)
               .withHttpHeaders(("Authentication", s"Bearer $accessToken"))
               .get
               .ifFailedThenToInfraError(s"error while getting $url")
               .map(_.json.toString)
    } yield decode[ConversationResponse](res)).ifLeftThenToInfraError(
      "error while converting conversation api response"
    )
  }
}

object UsersDaoImpl {
  case class ConversationResponse(channels: Seq[Channels])
  case class Channels(id: String)
}
