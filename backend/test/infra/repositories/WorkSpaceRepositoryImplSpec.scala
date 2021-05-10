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
import mockws.MockWS
import mockws.MockWSHelpers.Action
import play.api.inject.bind
import play.api.libs.ws.WSClient
import play.api.mvc.Results.Ok
import eu.timepit.refined.auto._
import infra.dao.slack.UsersDaoImpl.Member
import infra.dto.Tables
import infra.repositoryimpl.WorkSpaceRepositoryImpl
import infra.dto.Tables._
import io.circe.{Json, JsonObject}
import io.circe.syntax._
import org.scalacheck.Gen

import scala.concurrent.Future

class WorkSpaceRepositoryImplSpec extends RepositorySpec[WorkSpaceRepository]

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

    case ("POST", str: String)
        if str.matches("https://slack.com/api/conversations.join") =>
      val res = Json.fromJsonObject(
        JsonObject(
          "ok"      -> Json.fromBoolean(true),
          "channel" -> Json.fromJsonObject(
            JsonObject(
              "id"   -> Json.fromString("channelID1"),
              "name" -> Json.fromString("channelName")
            )
          )
        )
      )
      Action(Ok(res.noSpaces))

    case ("POST", str: String)
        if str.matches("https://slack.com/api/chat.postMessage") =>
      val res = Json.obj(
        "ok"      -> Json.fromBoolean(true),
        "channel" -> Json.fromString("testId")
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
    "succeed".which {
      "target bot exists" should {
        "new data added correctly" in {
          forAll(workSpaceGen, applicationGen, botGen, accessTokensGen) {
            (_workSpace, application, bot, accessToken) =>
              val workSpace = _workSpace.copy(bots =
                _workSpace.bots :+ bot.copy(
                  applicationId = application.id,
                  accessToken = accessToken
                )
              )

              db.run(WorkSpaces.delete)
              val result =
                repository.update(workSpace, application.id).futureValue

              val savedValue = db.run(WorkSpaces.result).futureValue
              assert(result === Some(()))
              assert(
                savedValue.head === WorkSpacesRow(
                  accessToken.value.value,
                  application.id.value.value,
                  workSpace.id.value.value
                )
              )
              assert(savedValue.length === 1)
          }
        }
      }

      "no target bot exists" should {
        "return None" in {
          forAll(workSpaceGen, applicationGen) { (workSpace, application) =>
            db.run(WorkSpaces.delete)
            val result =
              repository.update(workSpace, application.id).futureValue

            assert(result === None)
          }
        }
      }
    }
  }

  "joinChannels" when {
    "succeed" should {
      "return future unit" in {
        forAll(
          workSpaceGen,
          applicationGen,
          botGen,
          Gen.nonEmptyListOf(channelIdGen)
        ) { (_workSpace, application, bot, channelIds) =>
          val workSpace = _workSpace.copy(bots =
            _workSpace.bots :+ bot.copy(applicationId = application.id)
          )

          val result = repository
            .joinChannels(workSpace, application.id, channelIds)
            .futureValue
          assert(result === ())
        }
      }
    }
  }

  "removeBot" when {
    "succeed" should {
      "target data removed correctly" in {
        forAll(workSpaceGen, botGen) { (_workSpace, bot) =>
          val workSpace = _workSpace.copy(
            id = WorkSpaceId("workSpace1"),
            bots =
              _workSpace.bots :+ bot.copy(applicationId = ApplicationId("bot1"))
          )

          db.run(WorkSpaces.delete).futureValue
          db.run(insertAction).futureValue

          val result        = repository.removeBot(workSpace).futureValue
          val dbContent     = db.run(WorkSpaces.result).futureValue
          val targetContent = db
            .run(WorkSpaces.filter(_.teamId === "workSpace3").result)
            .futureValue

          assert(result === ())
          assert(dbContent.length === 2)
          assert(targetContent.length === 0)
        }
      }
    }
  }

  "sendMessage" when {
    "succeed".which {
      "target bot & its draft message exists" should {
        "return some unit" in {
          forAll(workSpaceGen, botGen, channelIdGen, botIdGen) {
            (_workSpace, _bot, channelId, botId) =>
              val bot       = _bot.copy(id = Some(botId)).createOnboardingMessage
              val workSpace = _workSpace.copy(bots = _workSpace.bots :+ bot)

              val result =
                repository.sendMessage(workSpace, botId, channelId).futureValue

              assert(result === Some())

          }
        }
      }

      "no target bot exists" should {
        "return None" in {
          forAll(workSpaceGen, channelIdGen, botIdGen) {
            (_workSpace, channelId, botId) =>
              val result =
                repository.sendMessage(_workSpace, botId, channelId).futureValue

              assert(result === None)
          }
        }
      }

      "no draft message exists" in {
        forAll(workSpaceGen, botGen, channelIdGen, botIdGen) {
          (_workSpace, _bot, channelId, botId) =>
            val bot       = _bot.copy(id = Some(botId), draftMessage = None)
            val workSpace = _workSpace.copy(bots = _workSpace.bots :+ bot)

            val result =
              repository.sendMessage(workSpace, botId, channelId).futureValue

            assert(result === None)
        }
      }
    }
  }
}

class WorkSpaceRepositoryImplFailSpec extends WorkSpaceRepositoryImplSpec {
  val mockWs = MockWS {
    case ("POST", str: String)
        if str.matches("https://slack.com/api/oauth.v2.access") =>
      Action(
        Ok(
          Json
            .fromJsonObject(
              JsonObject(
                "ok"    -> Json.fromBoolean(false),
                "error" -> Json.fromString("error")
              )
            )
            .noSpaces
        )
      )

    case ("POST", str: String)
        if str.matches("https://slack.com/api/conversations.join") =>
      val res = Json.fromJsonObject(
        JsonObject(
          "ok"    -> Json.fromBoolean(false),
          "error" -> Json.fromString("error")
        )
      )
      Action(Ok(res.noSpaces))

    case ("POST", str: String)
        if str.matches("https://slack.com/api/chat.postMessage") =>
      val res = Json.obj(
        "ok"    -> Json.fromBoolean(false),
        "error" -> Json.fromString("error")
      )
      Action(Ok(res.noSpaces))
  }

  override val app =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  implicit val conf: PatienceConfig = PatienceConfig(scaled(Span(1000, Millis)))

  "find(code, clientId, clientSecret)" when {
    "failed in oauth api" should {
      "return infra error" in {
        forAll(
          temporaryOauthCodeGen,
          applicationClientIdGen,
          applicationClientSecretGen
        ) { (code, clientId, clientSecret) =>
          val result = repository.find(code, clientId, clientSecret)

          val msg = """
              |APIError
              |error while bot access token decode in workSpaceRepository.find
              |Attempt to decode value on failed cursor: DownField(access_token)
              |""".stripMargin.trim

          whenReady(result.failed)(e => assert(msg === e.getMessage.trim))
        }
      }
    }
  }

  "joinChannels" when {
    "failed in conversationDao.join" should {
      "return infra error" in {
        forAll(
          workSpaceGen,
          applicationGen,
          botGen,
          Gen.nonEmptyListOf(channelIdGen)
        ) { (_workSpace, application, bot, channelIds) =>
          val workSpace = _workSpace.copy(bots =
            _workSpace.bots :+ bot.copy(applicationId = application.id)
          )

          val result =
            repository.joinChannels(workSpace, application.id, channelIds)

          val msg = """
              |DBError
              |error while WorkSpaceRepository.joinChannel
              |APIError
              |error while converting conversation join api response
              |Attempt to decode value on failed cursor: DownField(channel)
              |""".stripMargin.trim

          whenReady(result.failed)(e => assert(e.getMessage.trim === msg))
        }
      }
    }
  }

  "sendMessage" when {
    "failed in chatDao.postMessage" should {
      "return infra error" in {
        forAll(workSpaceGen, botGen, channelIdGen, botIdGen) {
          (_workSpace, _bot, channelId, botId) =>
            val bot       = _bot.copy(id = Some(botId)).createOnboardingMessage
            val workSpace = _workSpace.copy(bots = _workSpace.bots :+ bot)

            val result = repository.sendMessage(workSpace, botId, channelId)

            val msg = """
                |DBError
                |error while WorkSpaceRepository.sendMessage
                |APIError
                |error while converting list api response
                |Attempt to decode value on failed cursor: DownField(channel)
                |""".stripMargin.trim

            whenReady(result.failed)(e => assert(e.getMessage.trim === msg))
        }
      }
    }
  }
}
