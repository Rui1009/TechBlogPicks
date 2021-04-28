package infra.repositoryimpl

import com.google.inject.Inject
import domains.application.Application._
import cats.implicits._
import domains.bot.Bot
import domains.workspace.WorkSpace._
import domains.workspace.{WorkSpace, WorkSpaceRepository}
import domains.bot.Bot._
import domains.channel.{Channel, DraftMessage}
import domains.channel.Channel.ChannelId
import eu.timepit.refined.api.Refined
import infra.dao.slack._
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
  protected val usersDao: UsersDao,
  val conversationDao: ConversationDao,
  protected val chatDao: ChatDao
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile] with WorkSpaceRepository
    with API with AccessTokenPublisherTokenDecoder {
  override def find(
    code: WorkSpaceTemporaryOauthCode,
    clientId: ApplicationClientId,
    clientSecret: ApplicationClientSecret
  ): Future[Option[WorkSpace]] = {
    val oauthURL = "https://slack.com/api/oauth.v2.access"

    for {
      resp        <- ws.url(oauthURL)
                       .withQueryStringParameters(
                         "code"          -> code.value.value,
                         "client_id"     -> clientId.value.value,
                         "client_secret" -> clientSecret.value.value
                       )
                       .post(Json.Null.noSpaces)
                       .ifFailedThenToInfraError(s"error while posting $oauthURL")
      accessToken <-
        decode[BotAccessToken](resp.json.toString()).ifLeftThenToInfraError
      info        <- teamDao.info(accessToken.value.value)
      workSpace   <-
        find(WorkSpaceId(Refined.unsafeApply(info.team.id)))
          .ifFailedThenToInfraError(
            "error while workSpaceRepository.find in workSpaceRepository.find"
          )
    } yield workSpace.map(_.copy(unallocatedToken = Some(accessToken)))
  }

  override def find(id: WorkSpaceId): Future[Option[WorkSpace]] = (for {
    rows       <- db.run(WorkSpaces.filter(_.teamId === id.value.value).result)
    responses  <- findBotUser(rows.map(_.botId))
    channelIds <- findChannelIds(rows)

    channels = channelIds.flatMap { case (ids, _) =>
                 ids.map(i =>
                   Channel(i, Seq.empty)
                 ) // Todo: historyもきちんと取得してそれを返す
               }

    bots = responses.flatMap { res =>
             val maybeToken        = rows
               .find(_.botId == res.id)
               .map(row => BotAccessToken(Refined.unsafeApply(row.token)))
             val joinedChannelsIds =
               channelIds.filter(_._2 === res.id).flatMap(_._1)
             (for {
               appId <- res.apiAppId
               token <- maybeToken
             } yield Bot(
               Some(BotId(Refined.unsafeApply(res.id))),
               BotName(Refined.unsafeApply(res.name)),
               ApplicationId(Refined.unsafeApply(appId)),
               token,
               joinedChannelsIds,
               None
             )).toSeq
           }
  } yield
    if (rows.isEmpty) None else Some(WorkSpace(id, None, bots, channels, None)))
    .ifFailedThenToInfraError("error while WorkSpaceRepository.find")

  private def findBotUser(botIds: Seq[String]) = usersDao
    .list(sys.env.getOrElse("ACCESS_TOKEN", ""))
    .map(_.members.filter(m => m.isBot && Set(m.id).subsetOf(botIds.toSet)))

  private def findChannelIds(rows: Seq[WorkSpacesRow]) = Future.sequence(
    rows.map(a =>
      usersDao
        .conversations(a.token)
        .map(r =>
          (r.channels.map(c => ChannelId(Refined.unsafeApply(c))), a.botId)
        )
    )
  )

  override def update(
    model: WorkSpace,
    applicationId: ApplicationId
  ): Future[Option[Unit]] = model.bots
    .find(_.applicationId == applicationId)
    .map(_.accessToken.value.value) match {
    case Some(v) => db
        .run(
          WorkSpaces += WorkSpacesRow(
            v,
            applicationId.value.value,
            model.id.value.value
          )
        )
        .map(_ => Some())
        .ifFailedThenToInfraError("error while WorkSpaceRepository.update")
    case None    => Future.successful(None)
  }

  override def joinChannels(
    model: WorkSpace,
    applicationId: ApplicationId,
    channelIds: Seq[ChannelId]
  ): Future[Unit] = Future
    .sequence(for {
      channelId <- channelIds
      bot       <- model.bots.filter(_.applicationId == applicationId)
    } yield for {
      _ <-
        conversationDao.join(bot.accessToken.value.value, channelId.value.value)
    } yield ())
    .map(_ => ())
    .ifFailedThenToInfraError("error while WorkSpaceRepository.joinChannel")

  override def removeBot(model: WorkSpace): Future[Unit] = db
    .run(
      WorkSpaces
        .filter(_.teamId === model.id.value.value)
        .filter(!_.botId.inSet(model.bots.map(_.applicationId.value.value)))
        .delete
    )
    .map(_ => ())
    .ifFailedThenToInfraError("error while WorkSpaceRepository.removeBot")

  override def sendMessage(
    bot: Bot,
    channel: Channel,
    message: DraftMessage
  ): Future[Unit] = for {
    _ <- chatDao.postMessage(
           bot.accessToken.value.value,
           channel.id.value.value,
           message
         )
  } yield ()
}
