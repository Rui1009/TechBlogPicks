package infra.dao.slack

import com.google.inject.Inject
import infra.dao.slack.ChatDaoImpl.{PostMessageBody, PostMessageResponse}
import play.api.libs.ws.WSClient
import query.publishposts.PublishPostsView
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser._
import infra.syntax.all._

import scala.concurrent.{ExecutionContext, Future}

trait ChatDao {
  def postMessage(body: PostMessageBody): Future[PostMessageResponse]
}

class ChatDaoImpl @Inject() (ws: WSClient)(implicit ec: ExecutionContext)
    extends ChatDao {
  def postMessage(body: PostMessageBody): Future[PostMessageResponse] = {
    val url = "https://slack.com/api/chat.postMessage"
    ws.url(url)
      .post(body.asJson.noSpaces)
      .ifFailedThenToInfraError(s"error while posting $url")
      .map(res => decode[PostMessageResponse](res.json.toString))
      .ifLeftThenToInfraError("post message failed")
  }
}

object ChatDaoImpl {
  case class PostMessageBody(
    token: String,
    channel: String,
    text: Option[String]
  )

  object PostMessageBody {
    def fromViewModels(
      models: Seq[PublishPostsView],
      tempMsg: String
    ): Seq[PostMessageBody] =
      for {
        model   <- models
        channel <- model.channels
      } yield PostMessageBody(
        model.token,
        channel,
        model.posts.foldLeft(Option(tempMsg))((acc, curr) =>
          for {
            accText <- acc
          } yield curr.url match {
            case Some(v) => accText + "\n" + v
            case None    => accText
          }
        )
      )
  }

  case class PostMessageResponse(channel: String)
}
