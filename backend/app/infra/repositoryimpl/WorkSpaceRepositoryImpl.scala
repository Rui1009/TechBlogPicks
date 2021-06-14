package infra.repositoryimpl

import com.google.inject.Inject
import domains.application.Application._
import domains.bot.Bot
import domains.bot.Bot._
import domains.channel.Channel.ChannelId
import domains.channel.ChannelMessage.{
  ChannelMessageSenderUserId,
  ChannelMessageSentAt
}
import domains.channel._
import domains.workspace.WorkSpace._
import domains.workspace.{WorkSpace, WorkSpaceRepository}
import eu.timepit.refined.api.Refined
import infra.DBError
import infra.dao.slack._
import infra.dto.Tables._
import infra.format.AccessTokenPublisherTokenDecoder
import infra.syntax.all._
import io.circe.Json
import io.circe.parser._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.ws._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class WorkSpaceRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val ws: WSClient,
  protected val teamDao: TeamDao,
  protected val usersDao: UsersDao,
  protected val conversationDao: ConversationDao,
  protected val chatDao: ChatDao
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile] with WorkSpaceRepository
    with API with AccessTokenPublisherTokenDecoder {
  override def find(
    code: WorkSpaceTemporaryOauthCode,
    clientId: ApplicationClientId,
    clientSecret: ApplicationClientSecret
  ): Future[WorkSpace] = {
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
        decode[BotAccessToken](resp.json.toString()).ifLeftThenToInfraError(
          "error while bot access token decode in workSpaceRepository.find"
        )
      info        <- teamDao.info(accessToken.value.value)
      workSpace   <-
        find(WorkSpaceId(Refined.unsafeApply(info.team.id)))
          .ifFailedThenToInfraError(
            "error while workSpaceRepository.find in workSpaceRepository.find"
          )
    } yield workSpace match {
      case Some(v) => v.copy(unallocatedToken = Some(accessToken))
      case None    => WorkSpace(
          WorkSpaceId(Refined.unsafeApply(info.team.id)),
          Some(code),
          Seq(),
          Seq(),
          Some(accessToken)
        )
    }
  }

  override def findByConstToken(id: WorkSpaceId): Future[Option[WorkSpace]] =
    (for {
      rows      <- db.run(WorkSpaces.filter(_.teamId === id.value.value).result)
      responses <- findBotUserByConstToken(rows.map(_.botId))
      channels  <- findChannels(rows)

      bots = responses.flatMap { res =>
               val maybeToken = rows
                 .find(row => res.apiAppId.contains(row.botId))
                 .map(row => BotAccessToken(Refined.unsafeApply(row.token)))

               val joinedChannelsIds = channels
                 .filter(id => res.apiAppId.contains(id._2))
                 .map(_._1.id)

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
      if (rows.isEmpty) None
      else Some(WorkSpace(id, None, bots, channels.map(_._1).distinct, None)))
      .ifFailedThenToInfraError("error while WorkSpaceRepository.find")

  override def find(id: WorkSpaceId): Future[Option[WorkSpace]] = (for {
    rows      <- db.run(WorkSpaces.filter(_.teamId === id.value.value).result)
    responses <- findBotUser(rows.map(row => (row.botId, row.token)))
    channels  <- findChannels(rows)

    bots = responses.flatMap { res =>
             val maybeToken = rows
               .find(row => res.apiAppId.contains(row.botId))
               .map(row => BotAccessToken(Refined.unsafeApply(row.token)))

             val joinedChannelsIds =
               channels.filter(id => res.apiAppId.contains(id._2)).map(_._1.id)

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
    if (rows.isEmpty) None
    else Some(WorkSpace(id, None, bots, channels.map(_._1).distinct, None)))
    .ifFailedThenToInfraError("error while WorkSpaceRepository.find")

  private def findBotUser(botIdTokens: Seq[(String, String)]) = Future
    .sequence(for {
      botIdToken <- botIdTokens
    } yield for {
      botUser: Seq[UsersDaoImpl.Member] <-
        usersDao
          .list(botIdToken._2)
          .map(
            _.members.filter(m =>
              m.isBot && Set(m.apiAppId)
                .subsetOf(botIdTokens.map(bit => Some(bit._1)).toSet)
            )
          )
    } yield botUser)
    .map(_.flatten.distinct)

  private def findBotUserByConstToken(botIds: Seq[String]) = usersDao
    .list(sys.env.getOrElse("ACCESS_TOKEN", ""))
    .map(
      _.members.filter(m =>
        m.isBot && Set(m.apiAppId).subsetOf(botIds.map(id => Some(id)).toSet)
      )
    )

  private def findChannels(rows: Seq[WorkSpacesRow]) = Future
    .sequence(for {
      row <- rows
    } yield for {
      r <- usersDao
             .conversations(row.token, "public_channel,im")
             .ifFailedThenToInfraError(
               "error while usersDao.conversations in findChannels"
             )

    } yield for {
      channel <- r.channels
    } yield for {
      info <-
        conversationDao
          .info(row.token, channel.id)
          .ifFailedThenToInfraError(
            "error while conversationDao.info in findChannels"
          )
          .transformWith { //　上のエラー処理とまとめる
            case Success(Some(v)) => Future.successful(
                (
                  Channel(
                    ChannelId(Refined.unsafeApply(channel.id)),
                    Seq(
                      ChannelMessage(
                        ChannelMessageSentAt(Refined.unsafeApply(v.ts)),
                        ChannelMessageSenderUserId(
                          Refined.unsafeApply(v.senderUserId)
                        ),
                        v.text
                      )
                    )
                  ),
                  row.botId
                )
              )
            case Success(None)    => Future.successful(
                (
                  Channel(ChannelId(Refined.unsafeApply(channel.id)), Seq()),
                  row.botId
                )
              )
            case Failure(e)       => Future.failed(DBError(e.getMessage))
          }
    } yield info)
    .map(_.flatten)
    .map(v => Future.sequence(v))
    .flatten

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
        .map(_ => Some(()))
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
    workSpace: WorkSpace,
    applicationId: ApplicationId,
    channelId: ChannelId
  ): Future[Option[Unit]] =
    workSpace.bots.find(bot => bot.applicationId == applicationId) match {
      case Some(v) => v.draftMessage match {
          case Some(d) => chatDao
              .postMessage(v.accessToken.value.value, channelId.value.value, d)
              .map(_ => Some(()))
              .ifFailedThenToInfraError(
                "error while WorkSpaceRepository.sendMessage"
              )
          case None    => Future.successful(None)
        }
      case None    => Future.successful(None)
    }
}
