package infra.repositoryimpl

import com.google.inject.Inject
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import domains.accesstokenpublisher.{
  AccessTokenPublisher,
  AccessTokenPublisherRepository
}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.ws._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import infra.format.AccessTokenPublisherTokenDecoder
import io.circe.parser._
import infra.syntax.all._
import io.circe.Json

import scala.concurrent.{ExecutionContext, Future}

class AccessTokenPublisherRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val ws: WSClient
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile]
    with AccessTokenPublisherRepository with API
    with AccessTokenPublisherTokenDecoder {
  override def find(
    code: AccessTokenPublisher.AccessTokenPublisherTemporaryOauthCode
  ): Future[Option[AccessTokenPublisher]] = {
    val oauthURL = "https://slack.com/api/oauth.v2.access"

    for {
      resp <- ws.url(oauthURL)
                .withQueryStringParameters(
                  "code"          -> code.value.value,
                  "client_id"     -> "1857273131876.1857349108100",
                  "client_secret" -> "ce17821a5d59928d7b2cd433502c6eee"
                )
                .post(Json.Null.noSpaces)
                .ifFailedThenToInfraError(s"error while posting $oauthURL")
    } yield for {
      accessToken: AccessTokenPublisherToken <-
        decode[AccessTokenPublisherToken](
          resp.json.toString()
        ).ifLeftThenReturnNone
    } yield AccessTokenPublisher(accessToken, code)
  }
}
