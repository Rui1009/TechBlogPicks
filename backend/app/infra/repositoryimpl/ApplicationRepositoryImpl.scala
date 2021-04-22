package infra.repositoryimpl

import com.google.inject.Inject
import domains.application.Application.{
  ApplicationClientId,
  ApplicationClientSecret,
  ApplicationId,
  ApplicationName
}
import domains.application.Post.{PostAuthor, PostPostedAt, PostTitle, PostUrl}
import domains.application.{Application, ApplicationRepository, Post}
import eu.timepit.refined.api.Refined
import infra.dao.slack.{ConversationDao, UsersDao, UsersDaoImpl}
import infra.dto.Tables._
import infra.syntax.all._
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

    val postQ = BotsPosts
      .filter(_.botId === applicationId.value.value)
      .flatMap(botpost => Posts.filter(_.id === botpost.id))
      .result

    val clientInfoQ = BotClientInfo
      .findBy(_.botId)
      .apply(applicationId.value.value)
      .result
      .headOption

    (for {
      members                                   <- usersDao.list(sys.env.getOrElse("ACCESS_TOKEN", ""))
      targetMember: Option[UsersDaoImpl.Member] <-
        members.members.find(member =>
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
          ),
          post.map(pos =>
            Post(
              PostUrl(Refined.unsafeApply(pos.url)),
              PostTitle(Refined.unsafeApply(pos.title)),
              PostAuthor(Refined.unsafeApply(pos.author)),
              PostPostedAt(Refined.unsafeApply(pos.postedAt))
            )
          )
        )
      )
    }.ifFailedThenToInfraError(
      "error while ApplicationRepository.find"
    )).flatten
  }

  override def filter(
    applicationIds: Seq[ApplicationId]
  ): Future[Seq[Application]] = {
    
    val workSpaceQ = WorkSpaces.filter(_.)
    
    
    val postQ = BotsPosts
      .filter(_.botId.inSet(applicationIds.map(_.value.value)))
      .flatMap(botpost => Posts.filter(_.id === botpost.id))
      .result

    val clientInfoQ = BotClientInfo
      .filter(_.botId.inSet(applicationIds.map(_.value.value)))
      .result
      .headOption

    for {
      members                                <- usersDao.list(sys.env.getOrElse("ACCESS_TOKEN", ""))
      targetMembers: Seq[UsersDaoImpl.Member] =
        members.members.filter(member =>
          applicationIds.map(id => Some(id.value.value)).contains(member.botId)
        )
    } yield db.run {
      for {
        post            <- postQ
        maybeClientInfo <- clientInfoQ
      } yield targetMembers.map(member =>
        Application(
        )
      )
    }
  }

  override def update(application: Application): Future[Unit] = {
    val findQ   = BotClientInfo
      .findBy(_.botId)
      .apply(application.id.value.value)
      .result
      .headOption
    val insertQ = BotClientInfo += application.toClientInfoRow
    val updateQ = BotClientInfo.update(application.toClientInfoRow)

    (for {
      clientInfo <- db.run(findQ)
    } yield clientInfo match {
      case Some(_) => db.run(updateQ)
      case None    => db.run(insertQ)
    }).flatten
      .map(_ => ())
      .ifFailedThenToInfraError("error while ApplicationRepository.update")
  }

  override def add(
    post: Post,
    applicationIds: Seq[ApplicationId]
  ): Future[Unit] = {
    val nowUnix      = System.currentTimeMillis / 1000
    val newPost      = post.toRow(nowUnix)
    val postsInsertQ =
      Posts.returning(Posts.map(_.id)).into((_, id) => id) += newPost
    val query        = for {
      postId <- postsInsertQ
      news    = applicationIds.map(id => BotsPostsRow(0, id.value.value, postId))
      _      <- BotsPosts ++= news
    } yield ()

    db.run(query.transactionally)
  }.ifFailedThenToInfraError("error while ApplicationRepository.add")
}
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
