package infra.dao.slack

import com.google.inject.Inject
import domains.channel.DraftMessage
import domains.channel.DraftMessage._
import infra.APIError
import infra.dao.ApiDao
import infra.dao.slack.ChatDaoImpl.PostMessageResponse
import infra.syntax.all._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import play.api.libs.ws.WSClient
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}

trait ChatDao {
  def postMessage(
    token: String,
    channel: String,
    text: String
  ): Future[PostMessageResponse]

  def postMessage(
    token: String,
    channel: String,
    blocks: DraftMessage
  ): Future[PostMessageResponse]
}

class ChatDaoImpl @Inject() (ws: WSClient)(implicit ec: ExecutionContext)
    extends ApiDao(ws) with ChatDao {
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
            s"post message failed -> token: $token" + "\n" + e.getMessage + "\n" + res.json.toString
          )
        )
      )
      .anywaySuccess(PostMessageResponse.empty)
  }

  def postMessage(
    token: String,
    channel: String,
    blocks: DraftMessage
  ): Future[PostMessageResponse] = {
    val url                                                = "https://slack.com/api/chat.postMessage"
    println("post message in!")
    println(blocks)
    implicit val encodeSectionBlock: Encoder[MessageBlock] = Encoder.instance {
      case section: SectionBlock =>
        val commonJson = JsonObject.empty
          .add("type", Json.fromString("section"))
          .add(
            "text",
            Json.fromJsonObject(
              JsonObject.empty
                .add("type", Json.fromString("mrkdwn"))
                .add("text", Json.fromString(section.blockText.text.value))
            )
          )

        section.blockAccessory match {
          case Some(blockAccessory: AccessoryImage) => Json.fromJsonObject(
              commonJson.add(
                "accessory",
                Json.fromJsonObject(
                  JsonObject.empty
                    .add("type", Json.fromString("image"))
                    .add(
                      "image_url",
                      Json.fromString(blockAccessory.imageUrl.value)
                    )
                    .add(
                      "alt_text",
                      Json.fromString(blockAccessory.imageAltText)
                    )
                )
              )
            )
          case None                                 => Json.fromJsonObject(commonJson)
        }

      case action: ActionBlock => Json.obj(
          "type"     -> Json.fromString("actions"),
          "elements" -> Json.fromValues(action.actionBlockElements.map {
            case elem: ActionSelect => Json.obj(
                "type"        -> Json.fromString(elem.actionType.value),
                "placeholder" -> Json.obj(
                  "type"  -> Json.fromString("plain_text"),
                  "text"  -> Json.fromString(elem.placeholder.placeHolderText),
                  "emoji" -> Json.fromBoolean(elem.placeholder.placeHolderEmoji)
                ),
                "action_id"   -> Json.fromString(elem.actionId.value)
              )
          })
        )
    }

    println(
      blocks.blocks
        .map(_.asJson)
        .toString
        .patch(0, "[", 5)
        .patch(blocks.blocks.map(_.asJson).toString.length - 2, "]", 2)
    )
    (for {
      res <-
        ws.url(url)
          .withHttpHeaders("Authorization" -> s"Bearer $token")
          .withQueryStringParameters(
            "channel" -> channel,
            "blocks"  -> blocks.blocks
              .map(_.asJson)
              .toString
              .patch(0, "[", 5)
              .patch(blocks.blocks.map(_.asJson).toString.length - 2, "]", 2)
          )
          .post(Json.Null.noSpaces)
          .ifFailedThenToInfraError(s"error while posting $url")
      _    = println(res.json.toString)
    } yield decode[PostMessageResponse](res.json.toString))
      .ifLeftThenToInfraError("error while converting list api response")
  }
}

object ChatDaoImpl {
  case class PostMessageResponse(channel: String)
  object PostMessageResponse {
    def empty: PostMessageResponse = PostMessageResponse("")
  }
}
