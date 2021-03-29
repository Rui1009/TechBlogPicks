package infra.queryprocessor

import helpers.traits.QueryProcessorSpec
import infra.dao.slack.UsersDaoImpl._
import io.circe.{Json, JsonObject}
import io.circe.syntax._
import mockws.MockWS
import mockws.MockWSHelpers.Action
import org.scalatest.time.{Millis, Span}
import play.api.Application
import play.api.inject.bind
import play.api.libs.ws.WSClient
import play.api.mvc.Results.Ok
import query.bots.{BotsQueryProcessor, BotsView}
import infra.dto.Tables._

trait BotsQueryProcessorSuccessSpecContext {
  val members = Seq(
    Member("1", "SlackBot", false, true, None),
    Member("2", "front_end", true, false, Some("2")),
    Member("3", "deleted", true, true, Some("3")),
    Member("4", "back_end", true, false, Some("4"))
  )

  val seed = Seq(BotClientInfoRow("2", Some("clientId"), Some("clientSecret")))

  val mockWs = MockWS {
    case ("GET", str: String)
        if str.matches("https://slack.com/api/users.list") =>
      val res = Json.fromJsonObject(
        JsonObject(
          "ok"      -> Json.fromBoolean(true),
          "members" ->
            Json.fromValues(
              members.map(m =>
                Json.obj(
                  "id"      -> Json.fromString(m.id),
                  "name"    -> Json.fromString(m.name),
                  "deleted" -> Json.fromBoolean(m.deleted),
                  "is_bot"  -> Json.fromBoolean(m.isBot),
                  "profile" -> Json
                    .fromJsonObject(JsonObject("api_app_id" -> m.botId.asJson))
                )
              )
            )
        )
      )
      Action(Ok(res.noSpaces))
  }
}

class BotsQueryProcessorSuccessSpec
    extends QueryProcessorSpec[BotsQueryProcessor]
    with BotsQueryProcessorSuccessSpecContext {

  val beforeAction = DBIO.seq(BotClientInfo.forceInsertAll(seed))

  before(db.run(beforeAction.transactionally))
  after(db.run(BotClientInfo.delete).ready())

  override val app: Application =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  implicit val conf: PatienceConfig =
    PatienceConfig(scaled(Span(500, Millis)), scaled(Span(15, Millis)))

  "findAll" when {
    "succeed" should {
      "return BotsView" in {
        val result   = queryProcessor.findAll.futureValue
        val expected = Seq(
          BotsView("2", "front_end", Some("clientId"), Some("clientSecret")),
          BotsView("4", "back_end", None, None)
        )

        assert(result.length === expected.length)
        expected.foreach(bot => assert(result.contains(bot)))
      }
    }
  }
}

trait BotsQueryProcessorFailSpecContext {
  val mockWs = MockWS {
    case ("GET", str: String)
        if str.matches("https://slack.com/api/users.list") =>
      val res = Json.fromJsonObject(
        JsonObject(
          "ok"    -> Json.fromBoolean(false),
          "error" -> Json.fromString("account_inactive")
        )
      )
      Action(Ok(res.noSpaces))
  }
}

class BotsQueryProcessorFailSpec
    extends QueryProcessorSpec[BotsQueryProcessor]
    with BotsQueryProcessorFailSpecContext {

  override val app: Application =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  implicit val conf: PatienceConfig =
    PatienceConfig(scaled(Span(10000, Millis)), scaled(Span(15, Millis)))

  "findAll" when {
    "succeed" should {
      "return BotsView" in {
        val result = queryProcessor.findAll

        val msg = """
            |DBError
            |error while BotsQueryProcessorImpl.findAll
            |APIError
            |error while converting list api response
            |Attempt to decode value on failed cursor: DownField(members)
            |""".stripMargin.trim

        whenReady(result.failed)(e => assert(e.getMessage.trim === msg))
      }
    }
  }
}
