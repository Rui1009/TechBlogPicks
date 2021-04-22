package infra.repositoryimpl

import com.google.inject.Inject
import domains.application.Application.{
  ApplicationClientId,
  ApplicationClientSecret
}
import domains.workspace.WorkSpace._
import domains.workspace.{WorkSpace, WorkSpaceRepository}
import domains.bot.Bot._
import eu.timepit.refined.api.Refined
import infra.dao.slack.TeamDao
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.ws._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import infra.format.AccessTokenPublisherTokenDecoder
import io.circe.parser._
import infra.syntax.all._
import infra.dto.Tables._
import io.circe.Json
import eu.timepit.refined.auto._
import infra.dto.Tables

import scala.concurrent.{ExecutionContext, Future}

class WorkSpaceRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val ws: WSClient,
  protected val teamDao: TeamDao
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile] with WorkSpaceRepository
    with API with AccessTokenPublisherTokenDecoder {
  override def find(
    code: WorkSpaceTemporaryOauthCode,
    clientId: ApplicationClientId,
    clientSecret: ApplicationClientSecret
  ): Future[Option[WorkSpace]] = {
    val oauthURL = "https://slack.com/api/oauth.v2.access"

    (for {
      resp <- ws.url(oauthURL)
                .withQueryStringParameters(
                  "code"          -> code.value.value,
                  "client_id"     -> clientId.value.value,
                  "client_secret" -> clientSecret.value.value
                )
                .post(Json.Null.noSpaces)
                .ifFailedThenToInfraError(s"error while posting $oauthURL")
    } yield for {
      accessToken <-
        decode[BotAccessToken](resp.json.toString()).ifLeftThenReturnNone
    } yield for {
      info <- teamDao.info(accessToken.value.value)
    } yield WorkSpace(
      WorkSpaceId(Refined.unsafeApply(info.team.id)),
      Some(code),
      Seq(),
      Seq()
    )).flatMap {
      case Some(v) => v.map(workSpace => Some(workSpace))
      case None    => Future.successful(None)
    }
  }

  override def find(id: WorkSpaceId): Future[Option[WorkSpace]] =
    for {
      rows <- db.run(WorkSpaces.filter(_.teamId === id.value.value).result)
    } yield
      if (rows.isEmpty) None
      else Some(
        WorkSpace(
          id,
          rows.map(row => WorkSpaceToken(Refined.unsafeApply(row.token))),
          None,
          rows.map(row => BotId(Refined.unsafeApply(row.botId)))
        )
      )

  override def find(id: WorkSpaceId, botId: BotId): Future[Option[WorkSpace]] =
    for {
      rows <-
        db.run(
          WorkSpaces
            .filter(workSpaces =>
              workSpaces.teamId === id.value.value && workSpaces.botId === botId.value.value
            )
            .result
        ).ifFailedThenToInfraError("error while WorkSpaceRepository.find")
    } yield
      if (rows.isEmpty) None
      else Some(
        WorkSpace(
          id,
          rows.map(row => WorkSpaceToken(Refined.unsafeApply(row.token))),
          None,
          rows.map(row => BotId(Refined.unsafeApply(row.botId)))
        )
      )

  override def add(model: WorkSpace): Future[Unit] = {
    val rows = for {
      token <- model.tokens
      botId <- model.botIds
    } yield WorkSpacesRow(token.value.value, botId.value.value, model.id.value.value)

    db.run(WorkSpaces ++= rows)
      .map(_ => ())
      .ifFailedThenToInfraError("error while WorkSpaceRepository.update")
  }

  override def update(model: WorkSpace): Future[Unit] = db
    .run(
      WorkSpaces
        .filter(_.teamId === model.id.value.value)
        .filter(!_.botId.inSet(model.botIds.map(_.value.value)))
        .delete
    )
    .map(_ => ())
    .ifFailedThenToInfraError("error while WorkSpaceRepository.update")
}
