package infra.repositoryimpl

import com.google.inject.Inject
import domains.application.Application.{
  ApplicationClientId,
  ApplicationClientSecret,
  ApplicationId,
  ApplicationName
}
import domains.application.{Application, ApplicationRepository}
import domains.post.Post.PostId
import eu.timepit.refined.api.Refined
import infra.dao.slack.{ConversationDao, UsersDao, UsersDaoImpl}
import infra.dto.Tables
import infra.dto.Tables._
import infra.syntax.all._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.ws.WSClient
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API

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
      members                  <- usersDao.list(sys.env.getOrElse("ACCESS_TOKEN", ""))
      targetMembers             = members.members
                                    .filter(member =>
                                      applicationIds
                                        .map(id => Some(id.value.value))
                                        .contains(member.apiAppId)
                                    )
                                    .foldLeft(Seq[(String, String)]()) { (acc, cur) =>
                                      cur.apiAppId match {
                                        case Some(v) => acc :+ (v, cur.name)
                                        case None    => acc
                                      }
                                    }
      postIdsAndClientInfoList <- db.run {
                                    for {
                                      postIds     <- postQ
                                      clientInfos <- clientInfoQ
                                    } yield (postIds, clientInfos)
                                  }
      (postIds, clientInfos)    = postIdsAndClientInfoList

    } yield for {
      targetMember                <- targetMembers
      (targetAppId, targetAppName) = targetMember
      clientInfo                  <- clientInfos.find(info => targetAppId == info.botId).toSeq
    } yield Application(
      ApplicationId(Refined.unsafeApply(targetAppId)),
      ApplicationName(Refined.unsafeApply(targetAppName)),
      clientInfo.clientId.map(id =>
        ApplicationClientId(Refined.unsafeApply(id))
      ),
      clientInfo.clientSecret.map(secret =>
        ApplicationClientSecret(Refined.unsafeApply(secret))
      ),
      postIds.map(id => PostId(Refined.unsafeApply(id)))
    )).ifFailedThenToInfraError("error while ApplicationRepository.filter")
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
}
