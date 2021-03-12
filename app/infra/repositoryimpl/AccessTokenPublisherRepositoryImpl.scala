package infra.repositoryimpl

import com.google.inject.Inject
import domains.{DomainError, EmptyStringError}
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import domains.accesstokenpublisher.{AccessTokenPublisher, AccessTokenPublisherRepository}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.Json
import play.api.libs.ws._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import eu.timepit.refined.auto._

import scala.concurrent.{ExecutionContext, Future}

class AccessTokenPublisherRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val ws: WSClient
) (implicit val ec: ExecutionContext) extends HasDatabaseConfigProvider[PostgresProfile] with AccessTokenPublisherRepository
  with API {
  override def find(code: AccessTokenPublisher.AccessTokenPublisherTemporaryOauthCode): Future[AccessTokenPublisher] = {
    val oauthURL = "https://slack.com/api/oauth.v2.access"
    val postedParam = Json.obj(
      "code" -> code.value.value
    )

    for {
      resp <- ws.url(oauthURL).post(postedParam) // 通信が失敗した時のハンドリング
    } yield for {
      // 型キャストせずにcirceでレスポンス用のclassを作ってそいつにdecode / circeでjsonをdecodeできなかったらmatchでNoneで返す（エラーってこと）
      accessToken <- AccessTokenPublisherToken((resp.json \ "access_token").as[String])
      accessTokenPublisher = AccessTokenPublisher(accessToken, code)
    } yield accessTokenPublisher
  }
}
