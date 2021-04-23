package infra.repositories

import domains.workspace.WorkSpace.{WorkSpaceId, WorkSpaceToken}
import play.api.mvc.Results.Ok
import domains.bot.{Bot, BotRepository}
import domains.bot.Bot.{BotClientId, BotClientSecret, BotId, BotName}
import domains.post.Post.PostId
import helpers.traits.{HasDB, RepositorySpec}
import infra.dao.slack.UsersDaoImpl.Member
import infra.dto.Tables._
import play.api.inject.bind
import mockws.MockWS
import mockws.MockWSHelpers.Action
import play.api.Application
import play.api.libs.ws.WSClient
import eu.timepit.refined.auto._
import cats.syntax.option._
import infra.dto
import infra.dto.Tables
import io.circe.{Json, JsonObject}
import org.scalatest.time.{Millis, Span}

import scala.concurrent.Future

trait BotRepositoryImplSpecContext { this: HasDB =>
  val beforeAction = DBIO
    .seq(
      WorkSpaces.forceInsertAll(
        Seq(
          WorkSpacesRow("token1", "bot1", "team1"),
          WorkSpacesRow("token2", "bot1", "team2"),
          WorkSpacesRow("token3", "bot2", "team1")
        )
      ),
      Posts.forceInsertAll(
        Seq(
          PostsRow(1, "url1", "title", "daiki", 1, 2),
          PostsRow(2, "url2", "title2", "daiki", 2, 2),
          PostsRow(3, "url3", "title3", "daiki", 3, 2)
        )
      ),
      BotsPosts.forceInsertAll(
        Seq(
          BotsPostsRow(1, "bot1", 1),
          BotsPostsRow(2, "bot1", 2),
          BotsPostsRow(3, "bot1", 3),
          BotsPostsRow(4, "bot2", 1),
          BotsPostsRow(5, "bot3", 3)
        )
      ),
      BotClientInfo.forceInsertAll(
        Seq(BotClientInfoRow("bot1", "clientId".some, "clientSecret".some))
      )
    )
    .transactionally

  val deleteAction =
    BotsPosts.delete >> Posts.delete >> WorkSpaces.delete >> BotClientInfo.delete

  val paramBotId       = BotId("bot1")
  val paramWorkSpaceId = WorkSpaceId("team1")

}

class BotRepositoryImplSuccessSpec
    extends RepositorySpec[BotRepository] with BotRepositoryImplSpecContext {

  val members = Seq(
    Member("1", "SlackBot", false, true, None),
    Member("2", "front_end", true, false, Some("frontId")),
    Member("3", "deleted", true, true, Some("deletedId")),
    Member("4", "back_end", true, false, Some("bot1"))
  )

  val mockWs = MockWS {
    case ("POST", str: String)
        if str.matches("https://slack.com/api/users.info") =>
      Action(
        Ok(
          Json
            .fromJsonObject(
              JsonObject(
                "user" -> Json.obj("name" -> Json.fromString("mock_bot_name"))
              )
            )
            .noSpaces
        )
      )
    case ("GET", str: String)
        if str.matches("https://slack.com/api.users.list") =>
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
                  m.apiAppId match {
                    case Some(v) =>
                      "profile" -> Json.obj("api_app_id" -> Json.fromString(v))
                    case None    => "profile" -> Json.obj()
                  }
                )
              )
            )
        )
      )
      Action(Ok(res.noSpaces))

    case ("POST", str: String)
        if str.matches("https://slack.com/api/conversations.join") =>
      val res = Json.fromJsonObject(
        JsonObject(
          "ok"      -> Json.fromBoolean(true),
          "channel" -> Json
            .fromJsonObject(JsonObject("id" -> Json.fromString("channnelID")))
        )
      )
      Action(Ok(res.noSpaces))
  }

  override val app: Application =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  implicit val conf: PatienceConfig =
    PatienceConfig(scaled(Span(500, Millis)), scaled(Span(15, Millis)))

  before(db.run(beforeAction).futureValue)
  after(db.run(deleteAction).ready())

  "find" when {
    "success" should {
      "return Future[Some[Bot]]" in {
        val result = repository.find(paramBotId).futureValue

        assert(
          result === Some(
            Bot(
              paramBotId,
              BotName("back_end"),
              Seq(WorkSpaceToken("token1"), WorkSpaceToken("token2")),
              Seq(PostId(1L), PostId(2L), PostId(3L)),
              Seq(),
              BotClientId("clientId").some,
              BotClientSecret("clientSecret").some
            )
          )
        )
      }
    }

    "target bot does not exist in returned value" should {
      "return future successful none" in {
        val result = repository.find(BotId("non exists bot id")).futureValue

        assert(result === None)
      }
    }

  }

  "find(botId, workSpaceId)" when {
    "success" should {
      "return right bot" in {
        val result = repository.find(paramBotId, paramWorkSpaceId).futureValue

        assert(
          result === Some(
            Bot(
              paramBotId,
              BotName("back_end"),
              Seq(WorkSpaceToken("token1")),
              Seq(),
              Seq(),
              None,
              None
            )
          )
        )
      }
    }

    "target bot does not exist in returned value" should {
      "return future successful none" in {
        val result = repository.find(BotId("non exists bot id")).futureValue

        assert(result === None)
      }
    }
  }

  "update" when {
    "client info does not exist" should {
      "insert new record" in {
        forAll(botGen) { model =>
          val updated = model.copy(id = BotId("bot2"))
          repository.update(updated).futureValue

          val result =
            db.run(BotClientInfo.filter(_.botId === "bot2").result).futureValue

          assert(result.length === 1)
          assert(result.head.clientId === updated.clientId.map(_.value.value))
          assert(
            result.head.clientSecret === updated.clientSecret.map(_.value.value)
          )

          db.run(BotClientInfo.filter(_.botId === "bot2").delete).futureValue
        }
      }
    }

    "client info has already exist" should {
      "update record" in {
        forAll(botGen) { model =>
          val updated = model.copy(id = BotId("bot1"))
          repository.update(updated).futureValue

          val result =
            db.run(BotClientInfo.filter(_.botId === "bot1").result).futureValue

          assert(result.length === 1)
          assert(result.head.clientId === updated.clientId.map(_.value.value))
          assert(
            result.head.clientSecret === updated.clientSecret.map(_.value.value)
          )
        }
      }
    }
  }

  "join" when {
    "success" should {
      "return future unit" in {
        forAll(joinedBotGen) { bot =>
          val result: Unit = repository.join(bot).futureValue
          assert(result === ())
        }
      }
    }
  }

//  "update" when {
//    "success" should {
//      "delete target data".which {
//        "length is right" in {
//          repository.update(WorkSpaceToken("token1")).futureValue
//
//          val accessTokensColumnLen =
//            db.run(AccessTokens.length.result).futureValue
//
//          assert(accessTokensColumnLen === 2)
//        }
//
//        "value is right" in {
//          repository.update(WorkSpaceToken("token1")).futureValue
//
//          val rowWithTargetTokenLen = db
//            .run(AccessTokens.filter(_.token === "token").length.result)
//            .futureValue
//
//          assert(rowWithTargetTokenLen === 0)
//
//        }
//      }
//    }
//  }
}

class BotRepositoryImplFailSpec
    extends RepositorySpec[BotRepository] with BotRepositoryImplSpecContext {
  val mockWs = MockWS {
    case ("POST", str: String)
        if str.matches("https://slack.com/api/users.info") =>
      Action(
        Ok(Json.obj("error" -> Json.fromString("user_not_found")).noSpaces)
      )
    case ("POST", str: String)
        if str.matches("https://slack.com/api/conversations.join") =>
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

  before(db.run(beforeAction).futureValue)
  after(db.run(deleteAction).ready())

  "find" when {
    // slackのapiを叩いた際にエラーが返ってきた場合: {ok: false, error: invalid_cursor}
    "user not exists fail in user dao" should {
      "return fail & error message" in {
        val result = repository.find(paramBotId)

        val msg = """
                    |APIError
                    |error while converting list api response
                    |No content to map due to end-of-input
                    | at [Source: (String)""; line: 1, column: 0]
                    |""".stripMargin.trim

        whenReady(result.failed)(e => assert(e.getMessage.trim == msg))
      }
    }
  }

  "update" when {
    "insert existing client id" should {
      "return error" in {
        forAll(botGen) { model =>
          val updated = model
            .copy(id = BotId("bot2"), clientId = BotClientId("clientId").some)

          val result = repository.update(updated)

          val msg = """
              |DBError
              |error while BotRepository.update
              |ERROR: 重複したキー値は一意性制約"bot_client_info_client_id_key"違反となります
              |  詳細: キー (client_id)=(clientId) はすでに存在します。
              |""".stripMargin.trim

          whenReady(result.failed)(e => assert(e.getMessage.trim === msg))
        }
      }
    }

    "insert existing client secret" should {
      "return error" in {
        forAll(botGen) { model =>
          val updated = model.copy(
            id = BotId("bot2"),
            clientSecret = BotClientSecret("clientSecret").some
          )

          val result = repository.update(updated)

          val msg = """
                      |DBError
                      |error while BotRepository.update
                      |ERROR: 重複したキー値は一意性制約"bot_client_info_client_secret_key"違反となります
                      |  詳細: キー (client_secret)=(clientSecret) はすでに存在します。
                      |""".stripMargin.trim

          whenReady(result.failed)(e => assert(e.getMessage.trim === msg))
        }
      }
    }
  }

  "join" when {
    "fail in conversation dao" should {
      "return fail & error msg" in {
        forAll(joinedBotGen) { bot =>
          val result = repository.join(bot)

          val msg = """
              |DBError
              |error while BotRepository.join
              |APIError
              |error while converting conversation join api response
              |Attempt to decode value on failed cursor: DownField(channel)
              |""".stripMargin.trim

          whenReady(result.failed)(e => assert(e.getMessage.trim === msg))
        }
      }
    }
  }
}
