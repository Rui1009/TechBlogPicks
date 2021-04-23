package infra.repositoryimpl

import com.google.inject.Inject
import domains.application.Application.{
  ApplicationClientId,
  ApplicationClientSecret,
  ApplicationId
}
import cats.implicits._
import domains.bot.Bot
import domains.workspace.WorkSpace._
import domains.workspace.{WorkSpace, WorkSpaceRepository}
import domains.bot.Bot._
import domains.channel.Channel
import domains.channel.Channel.ChannelId
import eu.timepit.refined.api.Refined
import infra.dao.slack.{TeamDao, UsersDao}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.ws._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import infra.format.AccessTokenPublisherTokenDecoder
import io.circe.parser._
import io.circe.generic.auto._
import infra.syntax.all._
import infra.dto.Tables._
import io.circe.Json

import scala.concurrent.{ExecutionContext, Future}

class WorkSpaceRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val ws: WSClient,
  protected val teamDao: TeamDao,
  protected val usersDao: UsersDao
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

  override def find(id: WorkSpaceId): Future[Option[WorkSpace]] = for {
    rows       <- db.run(WorkSpaces.filter(_.teamId === id.value.value).result)
    responses  <- findBotUser(rows.map(_.botId))
    channelIds <- findChannelIds(rows)

    channels = channelIds.flatMap { case (ids, _) =>
                 ids.map(i => Channel(i, Seq.empty))
               }

    bots = responses.flatMap { res =>
             val token             = rows
               .find(_.botId == res.id)
               .map(row => BotAccessToken(Refined.unsafeApply(row.token)))
             val joinedChannelsIds =
               channelIds.filter(_._2 === res.id).flatMap(_._1)
             res.apiAppId
               .map(appId =>
                 Bot(
                   Some(BotId(Refined.unsafeApply(res.id))),
                   BotName(Refined.unsafeApply(res.name)),
                   ApplicationId(Refined.unsafeApply(appId)),
                   token,
                   joinedChannelsIds
                 )
               )
               .toSeq
           }
  } yield if (rows.isEmpty) None else Some(WorkSpace(id, None, bots, channels))

  private type ChannelIdsAndBotId = Seq[(Seq[ChannelId], String)]

  private def findBotUser(botIds: Seq[String]) = usersDao
    .list(sys.env.getOrElse("ACCESS_TOKEN", ""))
    .map(_.members.filter(m => m.isBot && Set(m.id).subsetOf(botIds.toSet)))

  private def findChannelIds(
    rows: Seq[WorkSpacesRow]
  ): Future[ChannelIdsAndBotId] = Future.sequence(
    rows.map(a =>
      usersDao
        .conversations(a.token)
        .map(r =>
          (r.channels.map(c => ChannelId(Refined.unsafeApply(c))), a.botId)
        )
    )
  )

//  override def find(id: WorkSpaceId, botId: BotId): Future[Option[WorkSpace]] =
//    for {
//      rows <-
//        db.run(
//          WorkSpaces
//            .filter(workSpaces =>
//              workSpaces.teamId === id.value.value && workSpaces.botId === botId.value.value
//            )
//            .result
//        ).ifFailedThenToInfraError("error while WorkSpaceRepository.find")
//    } yield
//      if (rows.isEmpty) None
//      else Some(
//        WorkSpace(
//          id,
//          rows.map(row => WorkSpaceToken(Refined.unsafeApply(row.token))),
//          None,
//          rows.map(row => BotId(Refined.unsafeApply(row.botId)))
//        )
//      )

//  override def add(model: WorkSpace): Future[Unit] = {
//    val rows = for {
//      token <- model.tokens
//      botId <- model.botIds
//    } yield WorkSpacesRow(token.value.value, botId.value.value, model.id.value.value)
//
//    db.run(WorkSpaces ++= rows)
//      .map(_ => ())
//      .ifFailedThenToInfraError("error while WorkSpaceRepository.update")
//  }
//
  override def update(model: WorkSpace): Future[Unit] = db
    .run(
      WorkSpaces
        .filter(_.teamId === model.id.value.value)
        .filter(!_.botId.inSet(model.bots.map(_.applicationId.value.value)))
        .delete
    )
    .map(_ => ())
    .ifFailedThenToInfraError("error while WorkSpaceRepository.update")

  override def add(model: WorkSpace): Future[Unit] = ???

  override def find(id: WorkSpaceId, botId: BotId): Future[Option[WorkSpace]] =
    ???
}
