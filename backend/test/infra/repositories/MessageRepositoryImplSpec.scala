package infra.repositories

import domains.message.Message.{
  AccessoryImage,
  ActionBlock,
  ActionSelect,
  BlockText,
  MessageChannelId,
  SectionBlock,
  SelectPlaceHolder
}
import play.api.mvc.Results.Ok
import domains.workspace.WorkSpace.WorkSpaceToken
import eu.timepit.refined.api.Refined
import helpers.traits.RepositorySpec
import io.circe.{Json, JsonObject}
import mockws.MockWS
import mockws.MockWSHelpers.Action
import org.scalatest.time.{Millis, Span}
import play.api.Application
import play.api.libs.ws.WSClient
import play.api.inject.bind
import eu.timepit.refined.auto._

class MessageRepositoryImplSuccessSpecWithNullLatest
    extends RepositorySpec[MessageRepository] {
  val mockWs = MockWS {
    case ("GET", str: String)
        if str.matches("https://slack.com/api/conversations.info") =>
      Action(
        Ok(
          Json
            .fromJsonObject(
              JsonObject(
                "ok"      -> Json.fromBoolean(true),
                "channel" ->
                  Json.fromJsonObject(
                    JsonObject(
                      "id"     -> Json.fromString("mockId"),
                      "name"   -> Json.fromString("general"),
                      "latest" -> Json.Null
                    )
                  )
              )
            )
            .noSpaces
        )
      )
    case ("POST", str: String)
        if str.matches("https://slack.com/api/chat.postMessage") =>
      Action(
        Ok(
          Json
            .fromJsonObject(
              JsonObject(
                "ok"      -> Json.fromBoolean(true),
                "channel" -> Json.fromString("general"),
                "ts"      -> Json.fromString("111.1111")
              )
            )
            .noSpaces
        )
      )
  }

  override val app: Application =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  implicit val conf: PatienceConfig =
    PatienceConfig(scaled(Span(500, Millis)), scaled(Span(15, Millis)))

  "isEmpty" when {
    "success with null latest" should {
      "return future true" in {
        val result = repository
          .isEmpty(WorkSpaceToken("token"), MessageChannelId("channelID"))
          .futureValue

        assert(result === true)
      }
    }
  }

  "add" when {
    "success" should {
      "return future unit" in {
        forAll(accessTokenGen, messageGen) { (token, message) =>
          val result =
            repository.add(token, message.channelId, message.blocks).futureValue

          assert(result === ())
        }
      }
    }
  }
}

class MessageRepositoryImplSpecWithNonNullLatest
    extends RepositorySpec[MessageRepository] {
  val mockWs = MockWS {
    case ("GET", str: String)
        if str.matches("https://slack.com/api/conversations.info") =>
      Action(
        Ok(
          Json
            .fromJsonObject(
              JsonObject(
                "ok"      -> Json.fromBoolean(true),
                "channel" ->
                  Json.fromJsonObject(
                    JsonObject(
                      "id"     -> Json.fromString("mockId"),
                      "name"   -> Json.fromString("general"),
                      "latest" -> Json.fromJsonObject(
                        JsonObject(
                          "type" -> Json.fromString("message"),
                          "ts"   -> Json.fromString("111.11111")
                        )
                      )
                    )
                  )
              )
            )
            .noSpaces
        )
      )
  }

  override val app: Application =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  implicit val conf: PatienceConfig =
    PatienceConfig(scaled(Span(500, Millis)), scaled(Span(15, Millis)))

  "isEmpty" when {
    "success with non null latest" should {
      "return future false" in {
        val result = repository
          .isEmpty(WorkSpaceToken("token"), MessageChannelId("channelID"))
          .futureValue

        assert(result === false)
      }
    }
  }
}

class MessageRepositoryImplFailSpec extends RepositorySpec[MessageRepository] {
  val mockWs                    = MockWS {
    case ("GET", str: String)
        if str.matches("https://slack.com/api/conversations.info") =>
      Action(
        Ok(
          Json
            .fromJsonObject(
              JsonObject(
                "ok"    -> Json.fromBoolean(false),
                "error" -> Json.fromString("channel_not_found")
              )
            )
            .noSpaces
        )
      )
    case ("POST", str: String)
        if str.matches("https://slack.com/api/chat.postMessage") =>
      Action(
        Ok(
          Json
            .fromJsonObject(
              JsonObject(
                "ok"    -> Json.fromBoolean(false),
                "error" -> Json.fromString("channel_not_found")
              )
            )
            .noSpaces
        )
      )
  }
  override val app: Application =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  implicit val conf: PatienceConfig =
    PatienceConfig(scaled(Span(500, Millis)), scaled(Span(15, Millis)))

  "isEmpty" when {
    "channel not exists fail in conversation info dao" should {
      "return fail & error message" in {
        val result = repository
          .isEmpty(WorkSpaceToken("token"), MessageChannelId("channelID"))

        val msg = """
                    |APIError
                    |error while converting conversation info api response
                    |Attempt to decode value on failed cursor: DownField(channel)
                    |""".stripMargin.trim

        whenReady(result.failed)(e => assert(e.getMessage.trim === msg))
      }
    }
  }

  "add" when {
    "fail in chat post message dao" in {
      forAll(accessTokenGen, messageGen) { (token, message) =>
        val result = repository.add(token, message.channelId, message.blocks)

        val msg = """
            |APIError
            |error while converting list api response
            |Attempt to decode value on failed cursor: DownField(channel)
            |""".stripMargin.trim

        whenReady(result.failed)(e => assert(e.getMessage.trim === msg))
      }
    }
  }
}
