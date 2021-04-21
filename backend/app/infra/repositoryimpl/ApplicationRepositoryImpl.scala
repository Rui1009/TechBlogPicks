package infra.repositoryimpl

import com.google.inject.Inject
import domains.application.Application.{
  ApplicationClientId,
  ApplicationClientSecret,
  ApplicationId,
  ApplicationName
}
import domains.application.{Application, ApplicationRepository}
import domains.bot.Bot.BotName
import domains.bot.{Bot, BotRepository}
import domains.post.Post.PostId
import eu.timepit.refined.api.Refined
import infra.dao.slack.{ConversationDao, UsersDao}
import infra.dto.Tables
import infra.dto.Tables.{BotClientInfo, BotsPosts, WorkSpaces}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.ws.WSClient
import slick.jdbc.PostgresProfile.API
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

class ApplicationRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val ws: WSClient,
  protected val usersDao: UsersDao,
  protected val conversationDao: ConversationDao
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile]
    with ApplicationRepository with API {
  override def find(
    applicationId: ApplicationId
  ): Future[Option[Application]] = {

    val workSpaceQ = WorkSpaces
      .filter(_.botId === applicationId.value.value)
      .map(_.token)
      .result

    val postQ = BotsPosts.filter(_.botId === applicationId.value.value).result

    val clientInfoQ = BotClientInfo
      .findBy(_.botId)
      .apply(applicationId.value.value)
      .result
      .headOption

    (for {
      members      <- usersDao.list(sys.env.getOrElse("ACCESS_TOKEN", ""))
      targetMember <- members.members.find(member =>
                        member.botId == Some(applicationId.value.value)
                      ) match {
                        case Some(v) => Future.successful(Some(v))
                        case None    => Future.successful(None)
                      }
    } yield db.run {
      for {
        workSpaces      <- workSpaceQ
        post            <- postQ
        maybeClientInfo <- clientInfoQ
      } yield targetMember.map(app =>
        Application(
          applicationId,
          ApplicationName(Refined.unsafeApply(app.name)),
          maybeClientInfo.flatMap(info =>
            info.clientId.map(id =>
              ApplicationClientId(Refined.unsafeApply(id))
            )
          ),
          maybeClientInfo.flatMap(info =>
            info.clientSecret.map(secret =>
              ApplicationClientSecret(Refined.unsafeApply(secret))
            )
          )
        )
//        Bot(
//          botId,
//          BotName(Refined.unsafeApply(bot.name)),
//          workSpaces.map(at => WorkSpaceToken(Refined.unsafeApply(at))),
//          postId.map(pid => PostId(Refined.unsafeApply(pid))),
//          Seq(),
//          maybeClientInfo.flatMap(info =>
//            info.clientId.map(id => BotClientId(Refined.unsafeApply(id)))
//          ),
//          maybeClientInfo.flatMap(info =>
//            info.clientSecret.map(secret =>
//              BotClientSecret(Refined.unsafeApply(secret))
//            )
//          )
//        )
      )
    }.ifFailedThenToInfraError("error while BotRepository.find")).flatten
  }
}
