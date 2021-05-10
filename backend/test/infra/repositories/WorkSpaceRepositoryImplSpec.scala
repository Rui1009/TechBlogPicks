package infra.repositories

import domains.application.Application
import domains.application.Application.ApplicationId
import domains.bot.Bot
import domains.bot.Bot.{BotAccessToken, BotId, BotName}
import domains.channel.Channel
import domains.channel.Channel.ChannelId
import domains.workspace.WorkSpace._
import domains.workspace.{WorkSpace, WorkSpaceRepository}
import helpers.traits.RepositorySpec
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.Application
import mockws.MockWS
import mockws.MockWSHelpers.Action
import play.api.inject.bind
import play.api.libs.ws.WSClient
import play.api.mvc.Results.Ok
import eu.timepit.refined.auto._
import infra.dao.slack.UsersDaoImpl.Member
import infra.repositoryimpl.WorkSpaceRepositoryImpl
import infra.dto.Tables._
import io.circe.{Json, JsonObject}
import io.circe.syntax._

import scala.concurrent.Future

class WorkSpaceRepositoryImplSpec
    extends RepositorySpec[WorkSpaceRepositoryImpl]

class WorkSpaceRepositoryImplSuccessSpec extends WorkSpaceRepositoryImplSpec {
  val members = Seq(
    Member("1", "SlackBot", false, true, None),
    Member("2", "front_end", true, false, Some("bot1")),
    Member("3", "deleted", true, true, Some("bot2")),
    Member("4", "back_end", true, false, Some("bot3"))
  )

  val mockWs = MockWS {
    case ("POST", str: String)
        if str.matches("https://slack.com/api/oauth.v2.access") =>
      Action(
        Ok(
          Json
            .fromJsonObject(
              JsonObject("access_token" -> Json.fromString("mock access token"))
            )
            .noSpaces
        )
      )
    case ("GET", str: String)
        if str.matches("https://slack.com/api/team.info") =>
      Action(
        Ok(
          Json
            .fromJsonObject(
              JsonObject(
                "team" -> Json.fromJsonObject(
                  JsonObject("id" -> Json.fromString("workSpace1"))
                )
              )
            )
            .noSpaces
        )
      )
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

    case ("GET", str: String)
        if str.matches("https://slack.com/api/users.conversations") =>
      val channels = Seq("channel1", "channel2")
      val res      = Json.fromJsonObject(
        JsonObject(
          "ok"       -> Json.fromBoolean(true),
          "channels" ->
            Json.fromValues(
              channels.map(s =>
                Json.obj(
                  "id"   -> Json.fromString(s),
                  "name" -> Json.fromString("test")
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

  val insertAction = DBIO.seq(
    WorkSpaces.forceInsertAll(
      Seq(
        WorkSpacesRow("token1", "bot1", "workSpace1"),
        WorkSpacesRow("token2", "bot2", "workSpace2"),
        WorkSpacesRow("token3", "bot3", "workSpace1")
      )
    )
  )

  "find(WorkSpaceId)" when {
    "succeed" should {
      "get work space" in {

        db.run(WorkSpaces.delete).futureValue
        db.run(insertAction).futureValue

        val result = repository.find(WorkSpaceId("workSpace1")).futureValue

        assert(
          result === Some(
            WorkSpace(
              WorkSpaceId("workSpace1"),
              None,
              Seq(
                Bot(
                  Some(BotId("2")),
                  BotName("front_end"),
                  ApplicationId("bot1"),
                  BotAccessToken("token1"),
                  Seq(ChannelId("channel1"), ChannelId("channel2")),
                  None
                ),
                Bot(
                  Some(BotId("4")),
                  BotName("back_end"),
                  ApplicationId("bot3"),
                  BotAccessToken("token3"),
                  Seq(ChannelId("channel1"), ChannelId("channel2")),
                  None
                )
              ),
              Seq(
                Channel(ChannelId("channel1"), Seq()),
                Channel(ChannelId("channel2"), Seq())
              ),
              None
            )
          )
        )
      }
    }
  }

  "find(code, clientId, clientSecret)" when {
    "succeed" should {
      "get work space" in {
        forAll(
          temporaryOauthCodeGen,
          applicationClientIdGen,
          applicationClientSecretGen
        ) { (code, clientId, clientSecret) =>
          db.run(WorkSpaces.delete).futureValue
          db.run(insertAction).futureValue

          val result = repository.find(code, clientId, clientSecret).futureValue
          assert(
            result === Some(
              WorkSpace(
                WorkSpaceId("workSpace1"),
                None,
                Seq(
                  Bot(
                    Some(BotId("2")),
                    BotName("front_end"),
                    ApplicationId("bot1"),
                    BotAccessToken("token1"),
                    Seq(ChannelId("channel1"), ChannelId("channel2")),
                    None
                  ),
                  Bot(
                    Some(BotId("4")),
                    BotName("back_end"),
                    ApplicationId("bot3"),
                    BotAccessToken("token3"),
                    Seq(ChannelId("channel1"), ChannelId("channel2")),
                    None
                  )
                ),
                Seq(
                  Channel(ChannelId("channel1"), Seq()),
                  Channel(ChannelId("channel2"), Seq())
                ),
                Some(BotAccessToken("mock access token"))
              )
            )
          )
        }
      }
    }
  }

  "update" when {
    "succeed" should {
      "new data added correctly" in {
        forAll(workSpaceGen, applicationGen, botGen) {
          (_workSpace, application, bot) =>
            val workSpace = _workSpace.copy(bots =
              _workSpace.bots :+ bot.copy(
                applicationId = application.id,
                accessToken = BotAccessToken("token")
              )
            )

            db.run(WorkSpaces.delete)
            val result =
              repository.update(workSpace, application.id).futureValue

            val savedValue = db.run(WorkSpaces.result).futureValue
            assert(result === ())
            assert(
              savedValue.head === WorkSpacesRow(
                "token",
                application.id.value.value,
                workSpace.id.value.value
              )
            )
            assert(savedValue.length === 1)
        }
      }
    }
  }
}

//  "find" when {
//    "succeed" should {
//      "get work space" in {
//        forAll(temporaryOauthCodeGen, botClientIdGen, botClientSecretGen) {
//          (code, clientId, clientSecret) =>
//            val result =
//              repository.find(code, clientId, clientSecret).futureValue
//
//            assert(
//              result === Some(
//                WorkSpace(
//                  WorkSpaceId("teamId"),
//                  Seq(WorkSpaceToken("mock access token")),
//                  Some(code),
//                  Seq()
//                )
//              )
//            )
//        }
//      }
//    }
//  }

//  "find(by id & botId)" when {
//    "succeed" should {
//      "get work space" in {
//        val beforeAction              = DBIO.seq(
//          WorkSpaces.forceInsertAll(
//            Seq(
//              WorkSpacesRow("token1", "bot1", "teamA"),
//              WorkSpacesRow("token2", "bot1", "teamB"),
//              WorkSpacesRow("token3", "bot2", "teamA")
//            )
//          )
//        )
//        val afterAction               = WorkSpaces.delete
//        db.run(beforeAction.transactionally).futureValue
//        val result: Option[WorkSpace] =
//          repository.find(WorkSpaceId("teamA"), BotId("bot2")).futureValue
//
//        assert(
//          result === Some(
//            WorkSpace(
//              WorkSpaceId("teamA"),
//              Seq(WorkSpaceToken("token3")),
//              None,
//              Seq(BotId("bot2"))
//            )
//          )
//        )
//        db.run(afterAction).futureValue
//      }
//    }
//  }

//  "add" when {
//    "succeed" should {
//      "add new data" in {
//        forAll(newWorkSpaceGen) { newModel =>
//          repository.joinChannels(newModel).futureValue
//
//          val result = db.run(WorkSpaces.result).futureValue
//
//          val expected = for {
//            token <- newModel.tokens
//            botId <- newModel.botIds
//          } yield WorkSpacesRow(token.value.value, botId.value.value, newModel.id.value.value)
//
//          assert(result === expected)
//
//          db.run(WorkSpaces.delete).futureValue
//        }
//      }
//    }
//  }

//  "update" when {
//    "succeed" should {
//      "delete data" in {
//        val beforeAction = DBIO.seq(
//          WorkSpaces.forceInsertAll(
//            Seq(
//              WorkSpacesRow("token1", "bot1", "team1"),
//              WorkSpacesRow("token2", "bot2", "team2"),
//              WorkSpacesRow("token3", "bot3", "team1")
//            )
//          )
//        )
//
//        val deleteAction = WorkSpaces.delete
//
//        db.run(beforeAction.transactionally).futureValue
//
//        val params     = WorkSpace(
//          WorkSpaceId("team1"),
//          Seq(WorkSpaceToken("token2"), WorkSpaceToken("token3")),
//          None,
//          Seq(BotId("bot2"), BotId("bot3"))
//        )
//        repository.joinChannels(params)
//        val workSpaces = db.run(WorkSpaces.result).futureValue
//
//        assert(workSpaces.length === 2)
//        assert(workSpaces.head.token === "token2")
//
//        db.run(deleteAction).futureValue
//
//      }
//    }
//  }
//}

//class WorkSpaceRepositoryImplFailSpec
//    extends RepositorySpec[WorkSpaceRepository] {
//  "find" when {
//    "failed" should {
//      "None returned" in {
//        forAll(temporaryOauthCodeGen, botClientIdGen, botClientSecretGen) {
//          (code, clientId, clientSecret) =>
//            val result = repository.find(code, clientId, clientSecret)
//
//            whenReady(result, timeout(Span(1, Seconds))) { e =>
//              assert(e === None)
//            }
//        }
//      }
//    }
//  }
//
//  "find(by id & botId)" when {
//    "failed" should {
//      "None returned" in {
//        forAll(workSpaceIdGen, botIdGen) { (workSpaceId, botId) =>
//          val result = repository.find(workSpaceId, botId)
//
//          whenReady(result, timeout(Span(1, Seconds))) { e =>
//            assert(e === None)
//          }
//        }
//      }
//    }
//  }
//}
