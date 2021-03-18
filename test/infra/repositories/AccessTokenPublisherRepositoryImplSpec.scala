package infra.repositories

import domains.accesstokenpublisher.AccessTokenPublisher._
import domains.accesstokenpublisher.{
  AccessTokenPublisher,
  AccessTokenPublisherRepository
}
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
import infra.repositoryimpl.AccessTokenPublisherRepositoryImpl

class AccessTokenPublisherRepositoryImplSpec
    extends RepositorySpec[AccessTokenPublisherRepositoryImpl]

class AccessTokenPublisherRepositoryImplSuccessSpec
    extends AccessTokenPublisherRepositoryImplSpec {
  val mockWs = MockWS {
    case ("POST", str: String)
        if str.matches("https://slack.com/api/oauth.v2.access") =>
      Action(Ok(Json.obj("access_token" -> "mock access token")))
  }

  override val app: Application =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  "find" when {
    "succeed" should {
      "get access token" in {
        forAll(temporaryOauthCodeGen) { code =>
          val result = repository.find(code).futureValue

          assert(
            result === Some(
              AccessTokenPublisher(
                AccessTokenPublisherToken("mock access token"),
                code
              )
            )
          )
        }
      }
    }
  }
}

class AccessTokenPublisherRepositoryImplFailSpec
    extends RepositorySpec[AccessTokenPublisherRepository] {
  "find" when {
    "failed" should {
      "None returned" in {
        forAll(temporaryOauthCodeGen) { code =>
          val result = repository.find(code)

          whenReady(result, timeout(Span(1, Seconds))) { e =>
            assert(e === None)
          }
        }
      }
    }
  }
}
