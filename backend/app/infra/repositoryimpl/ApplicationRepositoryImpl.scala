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
import domains.post.Post
import domains.post.Post.{PostAuthor, PostId, PostPostedAt, PostTitle, PostUrl}
import eu.timepit.refined.api.Refined
import infra.dao.slack.{ConversationDao, UsersDao, UsersDaoImpl}
import infra.dto
import infra.dto.Tables
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
    val postQ = BotsPosts
      .filter(_.botId === applicationId.value.value)
      .flatMap(botpost => Posts.filter(_.id === botpost.id))
      .map(_.id)
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
          member.apiAppId == Some(applicationId.value.value)
        ) match {
          case Some(v) => Future.successful(Some(v))
          case None    => Future.successful(None)
        }
    } yield db.run {
      for {
        postIds         <- postQ
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
          postIds.map(id => PostId(Refined.unsafeApply(id)))
        )
      )
    }.ifFailedThenToInfraError(
      "error while ApplicationRepository.find"
    )).flatten
  }

  override def filter(
    applicationIds: Seq[ApplicationId]
  ): Future[Seq[Application]] = {

    val postQ = BotsPosts
      .filter(_.botId.inSet(applicationIds.map(_.value.value)))
      .flatMap(botpost => Posts.filter(_.id === botpost.id))
      .map(_.id)
      .result

    val clientInfoQ = BotClientInfo
      .filter(_.botId.inSet(applicationIds.map(_.value.value)))
      .result

    (for {
      members      <- usersDao.list(sys.env.getOrElse("ACCESS_TOKEN", ""))
      targetMembers = members.members.filter(member =>
                        applicationIds
                          .map(id => Some(id.value.value))
                          .contains(member.apiAppId)
                      )
    } yield db.run {
      for {
        postIds         <- postQ
        maybeClientInfo <- clientInfoQ
      } yield targetMembers.map(targetMember =>
        Application(
          ApplicationId(Refined.unsafeApply(targetMember.apiAppId)),
          ApplicationName(
            Refined.unsafeApply(
              targetMembers
                .find(member =>
                  member.apiAppId == Some(targetMember.apiAppId.value.value)
                )
                .map(v => ApplicationName(Refined.unsafeApply(v.name)))
            )
          ),
          maybeClientInfo
            .find(info =>
              targetMember.apiAppId.value.value.contains(info.botId)
            )
            .map(v => ApplicationClientId(Refined.unsafeApply(v.clientId))),
          maybeClientInfo
            .find(info =>
              targetMember.apiAppId.value.value.contains(info.botId)
            )
            .map(v =>
              ApplicationClientSecret(Refined.unsafeApply(v.clientSecret))
            ),
          postIds.map(id => PostId(Refined.unsafeApply(id)))
        )
      )
    }.ifFailedThenToInfraError(
      "error while ApplicationRepository.filter"
    )).flatten
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

  override def save(
    applications: Seq[Application],
    postId: PostId
  ): Future[Unit] = {
    val news = applications.map(app =>
      BotsPostsRow(0, app.id.value.value, postId.value.value)
    )
    db.run(BotsPosts ++= news)
      .map(_ => ())
      .ifFailedThenToInfraError("error while ApplicationRepository.save")
  }

//  override def add(
//    post: Post,
//    applicationIds: Seq[ApplicationId]
//  ): Future[Unit] = {
//    val nowUnix      = System.currentTimeMillis / 1000
//    val newPost      = post.toRow(nowUnix)
//    val postsInsertQ =
//      Posts.returning(Posts.map(_.id)).into((_, id) => id) += newPost
//    val query        = for {
//      postId <- postsInsertQ
//      news    = applicationIds.map(id => BotsPostsRow(0, id.value.value, postId))
//      _      <- BotsPosts ++= news
//    } yield ()
//
//    db.run(query.transactionally)
//  }.ifFailedThenToInfraError("error while ApplicationRepository.add")
}
