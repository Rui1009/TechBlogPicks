package infra.repositoryimpl

import com.google.inject.Inject
import domains.workspace.WorkSpace.WorkSpaceToken
import domains.bot.Bot.{BotClientId, BotClientSecret, BotName}
import domains.bot.{Bot, BotRepository}
import domains.post.Post.PostId
import eu.timepit.refined.api.Refined
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import infra.dto.Tables._
import play.api.libs.ws.WSClient
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import infra.dao.slack.{UsersDao, UsersDaoImpl}
import infra.syntax.all._

import scala.concurrent.{ExecutionContext, Future, blocking}

class BotRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val ws: WSClient,
  protected val usersDao: UsersDao
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile] with BotRepository
    with API {
  override def find(botId: Bot.BotId): Future[Option[Bot]] = {

    val workSpaceQ =
      WorkSpaces.filter(_.botId === botId.value.value).map(_.token).result

    val postQ =
      BotsPosts.filter(_.botId === botId.value.value).map(_.postId).result

    val clientInfoQ =
      BotClientInfo.findBy(_.botId).apply(botId.value.value).result.headOption

    (for {
      members      <- usersDao.list(sys.env.getOrElse("ACCESS_TOKEN", ""))
      targetMember <- members.members.find(member =>
                        member.botId == Some(botId.value.value)
                      ) match {
                        case Some(v) => Future.successful(Some(v))
                        case None    => Future.successful(None)
                      }
    } yield db.run {
      for {
        workSpaces      <- workSpaceQ
        postId          <- postQ
        maybeClientInfo <- clientInfoQ
      } yield targetMember.map(bot =>
        Bot(
          botId,
          BotName(Refined.unsafeApply(bot.name)),
          workSpaces.map(at => WorkSpaceToken(Refined.unsafeApply(at))),
          postId.map(pid => PostId(Refined.unsafeApply(pid))),
          maybeClientInfo.flatMap(info =>
            info.clientId.map(id => BotClientId(Refined.unsafeApply(id)))
          ),
          maybeClientInfo.flatMap(info =>
            info.clientSecret.map(secret =>
              BotClientSecret(Refined.unsafeApply(secret))
            )
          )
        )
      )
    }.ifFailedThenToInfraError("error while BotRepository.find")).flatten
  }

  override def update(bot: Bot): Future[Unit] = {
    val findQ   =
      BotClientInfo.findBy(_.botId).apply(bot.id.value.value).result.headOption
    val insertQ = BotClientInfo += bot.toClientInfoRow
    val updateQ = BotClientInfo.update(bot.toClientInfoRow)

    (for {
      clientInfo <- db.run(findQ)
    } yield clientInfo match {
      case Some(_) => db.run(updateQ)
      case None    => db.run(insertQ)
    }).flatten
      .map(_ => ())
      .ifFailedThenToInfraError("error while BotRepository.update")
  }

  override def update(accessToken: WorkSpaceToken): Future[Unit] = for {
    _ <- db.run {
           WorkSpaces.filter(_.token === accessToken.value.value).delete
         }.ifFailedThenToInfraError(
           "error while BotRepository.update(accessToken)"
         )
  } yield ()
}
