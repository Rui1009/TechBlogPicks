package infra.repositoryimpl

import com.google.inject.Inject
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import domains.accesstokenpublisher.{
  AccessTokenPublisher,
  AccessTokenPublisherRepository
}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.Json
import play.api.libs.ws._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import infra.format.AccessTokenPublisherTokenDecoder
import io.circe.parser._
import infra.syntax.either._

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
    val oauthURL    = "https://slack.com/api/oauth.v2.access"
    val postedParam = Json.obj("code" -> code.value.value)

    (for {
      resp <- ws.url(oauthURL).post(postedParam) //Todo: 通信が失敗した時のハンドリング
    } yield for {
      accessToken <- decode[AccessTokenPublisherToken](
                       resp.json.toString()
                     ).ifFailedThenReturnNone
    } yield for {
      accessTokenPublisher <- accessToken.map(AccessTokenPublisher(_, code))
    } yield accessTokenPublisher).flatten
  }
}
