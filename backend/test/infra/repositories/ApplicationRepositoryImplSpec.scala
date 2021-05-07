package infra.repositories

import domains.application.Application
import domains.application.Application.{
  ApplicationClientId,
  ApplicationClientSecret,
  ApplicationId,
  ApplicationName
}
import domains.post.Post.PostId
import helpers.traits.RepositorySpec
import infra.dao.slack.UsersDaoImpl.Member
import infra.repositoryimpl.ApplicationRepositoryImpl
import infra.dto.Tables._
import io.circe.{Json, JsonObject}
import io.circe.syntax._
import mockws.MockWS
import mockws.MockWSHelpers.Action
import play.api.mvc.Results.Ok
import eu.timepit.refined.auto._
import org.scalacheck.Gen
import org.scalatest.time.{Millis, Span}
import play.api.libs.ws.WSClient
import play.api.inject.bind

class ApplicationRepositoryImplSpec
    extends RepositorySpec[ApplicationRepositoryImpl]

class ApplicationRepositoryImplSuccessSpec
    extends ApplicationRepositoryImplSpec {
  val members = Seq(
    Member("1", "SlackBot", false, true, None),
    Member("2", "front_end", true, false, Some("bot1")),
    Member("3", "deleted", true, true, Some("bot2")),
    Member("4", "back_end", true, false, Some("bot3"))
  )
  val mockWs  = MockWS {
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
                  "profile" -> Json.fromJsonObject(
                    JsonObject("api_app_id" -> m.apiAppId.asJson)
                  )
                )
              )
            )
        )
      )
      Action(Ok(res.noSpaces))
  }

  override val app =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  implicit val conf: PatienceConfig = PatienceConfig(scaled(Span(1000, Millis)))

  "find" when {
    "succeed" should {
      "get application" in {
        val beforeAction = DBIO.seq(
          Posts.forceInsertAll(
            Seq(
              PostsRow(1, "https://yahoo.com", "yahoo", "yahoo", 100, 100),
              PostsRow(
                2,
                "https://pornhub.com",
                "pornhub",
                "pornhub",
                102,
                102
              ),
              PostsRow(3, "https://google.com", "google", "google", 103, 103)
            )
          ),
          BotsPosts.forceInsertAll(
            Seq(
              BotsPostsRow(1, "bot1", 1),
              BotsPostsRow(2, "bot2", 2),
              BotsPostsRow(3, "bot1", 3)
            )
          ),
          BotClientInfo.forceInsertAll(
            Seq(
              BotClientInfoRow("bot1", Some("client1"), Some("clientSecret1")),
              BotClientInfoRow("bot2", Some("client2"), Some("clientSecret2")),
              BotClientInfoRow("bot3", Some("client3"), Some("clientSecret3"))
            )
          )
        )

        val afterAction =
          BotsPosts.delete >> Posts.delete >> BotClientInfo.delete

        db.run(afterAction).futureValue
        db.run(beforeAction.transactionally).futureValue

        val result = repository.find(ApplicationId("bot1")).futureValue

        assert(
          result === Some(
            Application(
              ApplicationId("bot1"),
              ApplicationName("front_end"),
              Some(ApplicationClientId("client1")),
              Some(ApplicationClientSecret("clientSecret1")),
              Seq(PostId(1L), PostId(3L))
            )
          )
        )
      }
    }
  }

  "filter" when {
    "succeed" should {
      "filter applications correctly" in {
        val insertAction = DBIO.seq(
          Posts.forceInsertAll(
            Seq(
              PostsRow(1, "https://yahoo.com", "yahoo", "yahoo", 100, 100),
              PostsRow(
                2,
                "https://pornhub.com",
                "pornhub",
                "pornhub",
                102,
                102
              ),
              PostsRow(3, "https://google.com", "google", "google", 103, 103)
            )
          ),
          BotsPosts.forceInsertAll(
            Seq(
              BotsPostsRow(1, "bot1", 1),
              BotsPostsRow(2, "bot3", 2),
              BotsPostsRow(3, "bot1", 3)
            )
          ),
          BotClientInfo.forceInsertAll(
            Seq(
              BotClientInfoRow("bot1", Some("client1"), Some("clientSecret1")),
              BotClientInfoRow("bot2", Some("client2"), Some("clientSecret2")),
              BotClientInfoRow("bot3", Some("client3"), Some("clientSecret3"))
            )
          )
        )

        val deleteAction =
          BotsPosts.delete >> Posts.delete >> BotClientInfo.delete
        db.run(deleteAction).futureValue
        db.run(insertAction.transactionally).futureValue

        val result = repository
          .filter(Seq(ApplicationId("bot1"), ApplicationId("bot3")))
          .futureValue

        assert(
          result === Seq(
            Application(
              ApplicationId("bot1"),
              ApplicationName("front_end"),
              Some(ApplicationClientId("client1")),
              Some(ApplicationClientSecret("clientSecret1")),
              Seq(PostId(1L), PostId(3L))
            ),
            Application(
              ApplicationId("bot3"),
              ApplicationName("back_end"),
              Some(ApplicationClientId("client3")),
              Some(ApplicationClientSecret("clientSecret3")),
              Seq(PostId(2L))
            )
          )
        )
      }
    }
  }

  "update" when {
    "succeed" should {
      "target application updated correctly".which {
        "length and value is right" in {
          forAll(applicationGen) { application =>
            val insertAction = DBIO.seq(
              BotClientInfo.forceInsertAll(
                Seq(
                  BotClientInfoRow(
                    "bot1",
                    Some("client1"),
                    Some("clientSecret1")
                  ),
                  BotClientInfoRow(
                    "bot2",
                    Some("client2"),
                    Some("clientSecret2")
                  ),
                  BotClientInfoRow(
                    "bot3",
                    Some("client3"),
                    Some("clientSecret3")
                  )
                )
              )
            )

            val deleteAction = BotClientInfo.delete

            db.run(deleteAction).futureValue
            db.run(insertAction.transactionally).futureValue

            val paramApp = application.copy(id = ApplicationId("bot3"))
            repository.update(paramApp).futureValue

            val botClientInfo       = db.run(BotClientInfo.result).futureValue
            val targetBotClientInfo = db
              .run(BotClientInfo.filter(_.botId === "bot3").result)
              .futureValue

            assert(botClientInfo.length === 3)
            assert(
              targetBotClientInfo.head.clientId === paramApp.clientId
                .map(_.value.value)
            )
            assert(
              targetBotClientInfo.head.clientSecret === paramApp.clientSecret
                .map(_.value.value)
            )
          }
        }
      }

      "added new data correctly".which {
        "length and value is right" in {
          forAll(applicationGen) { application =>
            db.run(BotClientInfo.delete).futureValue

            repository.update(application).futureValue

            val botClientInfo = db.run(BotClientInfo.result).futureValue
            assert(botClientInfo.length === 1)
            assert(
              botClientInfo.head.botId === application.id.value.value && botClientInfo.head.clientId === application.clientId
                .map(
                  _.value.value
                ) && botClientInfo.head.clientSecret === application.clientSecret
                .map(_.value.value)
            )
          }
        }
      }
    }
  }

  "save" when {
    "succeed" should {
      "new botsPostsRows are added" in {
        forAll(Gen.nonEmptyListOf(applicationGen), postIdGen) {
          (applications, postId) =>
            val insertAction = DBIO.seq(
              Posts.forceInsertAll(
                Seq(
                  PostsRow(
                    postId.value.value,
                    "https://yahoo.com",
                    "yahoo",
                    "yahoo",
                    100,
                    100
                  )
                )
              )
            )
            db.run(BotsPosts.delete >> Posts.delete).futureValue
            db.run(insertAction).futureValue

            repository.save(applications, postId).futureValue
            val botsPosts = db.run(BotsPosts.result).futureValue
            assert(botsPosts.length === applications.length)

            assert(botsPosts.head.botId === applications.last.id.value.value)
        }
      }
    }
  }
}
