package infra.dao.slack

import infra.dao.slack.UsersDao.ConversationResponse
import play.api.libs.ws.WSClient
import io.circe.parser._
import io.circe.generic.auto._
import infra.syntax.all._

import scala.concurrent.{ExecutionContext, Future}

class UsersDao(ws: WSClient, accessToken: String)(implicit
  val ec: ExecutionContext
) {
  def conversations(id: String): Future[ConversationResponse] = {
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

object UsersDao {
  case class ConversationResponse(channels: Seq[Channels])
  case class Channels(id: String)
}
