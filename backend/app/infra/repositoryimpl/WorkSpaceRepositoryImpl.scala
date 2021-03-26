package infra.repositoryimpl

import com.google.inject.Inject
import domains.workspace.WorkSpace.WorkSpaceToken
import domains.workspace.{WorkSpace, WorkSpaceRepository}
import domains.bot.Bot.{BotClientId, BotClientSecret}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.ws._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import infra.format.AccessTokenPublisherTokenDecoder
import io.circe.parser._
import infra.syntax.all._
import infra.dto.Tables._
import io.circe.Json

import scala.concurrent.{ExecutionContext, Future}

class WorkSpaceRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val ws: WSClient
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile] with WorkSpaceRepository
    with API with AccessTokenPublisherTokenDecoder {
  override def find(
    code: WorkSpace.WorkSpaceTemporaryOauthCode,
    clientId: BotClientId,
    clientSecret: BotClientSecret
  ): Future[Option[WorkSpace]] = {
    val oauthURL = "https://slack.com/api/oauth.v2.access"

    for {
      resp <- ws.url(oauthURL)
                .withQueryStringParameters(
                  "code"          -> code.value.value,
                  "client_id"     -> clientId.value.value,
                  "client_secret" -> clientSecret.value.value
                )
                .post(Json.Null.noSpaces)
                .ifFailedThenToInfraError(s"error while posting $oauthURL")
    } yield for {
      accessToken: WorkSpaceToken <-
        decode[WorkSpaceToken](resp.json.toString()).ifLeftThenReturnNone
    } yield WorkSpace(accessToken, code)
  }
}
