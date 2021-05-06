package infra.repositories

import domains.application.Application
import helpers.traits.RepositorySpec
import infra.dao.slack.UsersDaoImpl.Member
import infra.repositoryimpl.ApplicationRepositoryImpl
import io.circe.{Json, JsonObject}
import io.circe.syntax._
import mockws.MockWS
import mockws.MockWSHelpers.Action
import play.api.mvc.Results.Ok

class ApplicationRepositoryImplSpec
    extends RepositorySpec[ApplicationRepositoryImpl]

class ApplicationRepositoryImplSuccessSpec
    extends ApplicationRepositoryImplSpec {
  val members = Seq(
    Member("1", "SlackBot", false, true, None),
    Member("2", "front_end", true, false, Some("2")),
    Member("3", "deleted", true, true, Some("3")),
    Member("4", "back_end", true, false, Some("4"))
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
  "find" when {
    "succeed" should {
      "get application" in {
        forAll(
          temporaryOauthCodeGen,
          applicationClientIdGen,
          applicationClientSecretGen,
          applicationGen
        ) { (code, clientId, clientSecret, application) =>
          val result = repository.find(application.id).futureValue

          assert(result === Some(Application(application.id)))

        }
      }
    }
  }
}
