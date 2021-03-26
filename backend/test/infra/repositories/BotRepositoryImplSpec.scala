package infra.repositories

import domains.workspace.WorkSpace.WorkSpaceToken
import play.api.mvc.Results.Ok
import domains.bot.{Bot, BotRepository}
import domains.bot.Bot.{BotClientId, BotClientSecret, BotId, BotName}
import domains.post.Post.PostId
import helpers.traits.{HasDB, RepositorySpec}
import infra.dto.Tables._
import play.api.inject.bind
import mockws.MockWS
import mockws.MockWSHelpers.Action
import play.api.Application
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import eu.timepit.refined.auto._
import cats.syntax.option._
import infra.dto
import infra.dto.Tables
import org.scalatest.time.{Millis, Span}

import scala.concurrent.Future

trait BotRepositoryImplSpecContext { this: HasDB =>
  val beforeAction = DBIO
    .seq(
      AccessTokens.forceInsertAll(
        Seq(
          AccessTokensRow("token1", "bot1"),
          AccessTokensRow("token2", "bot1"),
          AccessTokensRow("token3", "bot2")
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
    BotsPosts.delete >> Posts.delete >> AccessTokens.delete >> BotClientInfo.delete

  val paramBotId = BotId("bot1")

}

class BotRepositoryImplSuccessSpec
    extends RepositorySpec[BotRepository] with BotRepositoryImplSpecContext {

  val mockWs = MockWS {
    case ("POST", str: String)
        if str.matches("https://slack.com/api/users.info") =>
      Action(Ok(Json.obj("user" -> Json.obj("name" -> "mock_bot_name"))))
  }

  override val app: Application =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  before(db.run(beforeAction).futureValue)
  after(db.run(deleteAction).ready())

  "find" when {
    "success" should {
      "return Future[Bot]" in {
        val result = repository.find(paramBotId).futureValue

        assert(
          result === Bot(
            paramBotId,
            BotName("mock_bot_name"),
            Seq(WorkSpaceToken("token1"), WorkSpaceToken("token2")),
            Seq(PostId(1L), PostId(2L), PostId(3L)),
            BotClientId("clientId").some,
            BotClientSecret("clientSecret").some
          )
        )
      }
    }
  }

  "update" when {
    "success" should {
      "add new data".which {
        "length is right" in {
          forAll(botGen, accessTokenPublisherGen) {
            (bot, accessTokenPublisher) =>
              db.run(AccessTokens.delete).futureValue
              repository
                .update(bot, accessTokenPublisher.publishToken)
                .futureValue

              val accessTokenColumnLen =
                db.run(AccessTokens.length.result).futureValue
              assert(accessTokenColumnLen === 1)
          }
        }
        "value is right" in {
          forAll(botGen, accessTokenPublisherGen) {
            (bot, accessTokenPublisher) =>
              db.run(AccessTokens.delete).futureValue
              repository
                .update(bot, accessTokenPublisher.publishToken)
                .futureValue

              val accessTokensRow = db
                .run(AccessTokens.result.head)
                .map(r => AccessTokensRow(r.token, r.botId))
                .futureValue

              assert(accessTokensRow.botId === bot.id.value.value)
              assert(
                accessTokensRow.token === accessTokenPublisher.token.value.value
              )

          }
        }
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

  "update" when {
    "success" should {
      "delete target data".which {
        "length is right" in {
          repository.update(WorkSpaceToken("token1")).futureValue

          val accessTokensColumnLen =
            db.run(AccessTokens.length.result).futureValue

          assert(accessTokensColumnLen === 2)
        }

        "value is right" in {
          repository.update(WorkSpaceToken("token1")).futureValue

          val rowWithTargetTokenLen = db
            .run(AccessTokens.filter(_.token === "token").length.result)
            .futureValue

          assert(rowWithTargetTokenLen === 0)

        }
      }
    }
  }
}

class BotRepositoryImplFailSpec
    extends RepositorySpec[BotRepository] with BotRepositoryImplSpecContext {
  val mockWs = MockWS {
    case ("POST", str: String)
        if str.matches("https://slack.com/api/users.info") =>
      Action(Ok(Json.obj("error" -> "user_not_found")))
  }

  override val app: Application =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  implicit val conf: PatienceConfig =
    PatienceConfig(scaled(Span(500, Millis)), scaled(Span(15, Millis)))

  before(db.run(beforeAction).futureValue)
  after(db.run(deleteAction).ready())

  "find" when {
    "user not exists fail" should {
      "return fail & error message" in {
        val result = repository.find(paramBotId)

        val msg = """
                    |APIError
                    |error while converting info api response
                    |Attempt to decode value on failed cursor: DownField(user)
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
}
