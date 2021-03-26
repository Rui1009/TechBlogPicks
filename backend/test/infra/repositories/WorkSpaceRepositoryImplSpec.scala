package infra.repositories

import domains.workspace.WorkSpace._
import domains.workspace.{WorkSpace, WorkSpaceRepository}
import helpers.traits.RepositorySpec
import play.api.libs.json.Json
import org.scalatest.time.{Seconds, Span}
import play.api.Application
import mockws.MockWS
import mockws.MockWSHelpers.Action
import play.api.inject.bind
import play.api.libs.ws.WSClient
import play.api.mvc.Results.Ok
import eu.timepit.refined.auto._
import infra.repositoryimpl.WorkSpaceRepositoryImpl

class WorkSpaceRepositoryImplSpec
    extends RepositorySpec[WorkSpaceRepositoryImpl]

class WorkSpaceRepositoryImplSuccessSpec extends WorkSpaceRepositoryImplSpec {
  val mockWs = MockWS {
    case ("POST", str: String)
        if str.matches("https://slack.com/api/oauth.v2.access") =>
      Action(Ok(Json.obj("access_token" -> "mock access token")))
    case ("GET", str: String)
        if str.matches("https://slack.com/api/team.info") =>
      Action(Ok(Json.obj("team" -> Json.obj("id" -> "teamId"))))
  }

  override val app: Application =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  "find" when {
    "succeed" should {
      "get work space" in {
        forAll(temporaryOauthCodeGen, botClientIdGen, botClientSecretGen) {
          (code, clientId, clientSecret) =>
            val result =
              repository.find(code, clientId, clientSecret).futureValue

            assert(
              result === Some(
                WorkSpace(
                  WorkSpaceId("teamId"),
                  Seq(WorkSpaceToken("mock access token")),
                  Some(code),
                  Seq()
                )
              )
            )
        }
      }
    }
  }
}

class WorkSpaceRepositoryImplFailSpec
    extends RepositorySpec[WorkSpaceRepository] {
  "find" when {
    "failed" should {
      "None returned" in {
        forAll(temporaryOauthCodeGen, botClientIdGen, botClientSecretGen) {
          (code, clientId, clientSecret) =>
            val result = repository.find(code, clientId, clientSecret)

            whenReady(result, timeout(Span(1, Seconds))) { e =>
              assert(e === None)
            }
        }
      }
    }
  }
}
