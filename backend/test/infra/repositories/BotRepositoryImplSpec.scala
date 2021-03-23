package infra.repositories

import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import play.api.mvc.Results.Ok
import domains.bot.{Bot, BotRepository}
import domains.bot.Bot.{BotId, BotName}
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
import infra.dto.Tables

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
      )
    )
    .transactionally

  val deleteAction = BotsPosts.delete >> Posts.delete >> AccessTokens.delete

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
            Seq(
              AccessTokenPublisherToken("token1"),
              AccessTokenPublisherToken("token2")
            ),
            Seq(PostId(1L), PostId(2L), PostId(3L))
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
}
