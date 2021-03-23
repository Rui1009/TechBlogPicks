package infra.repositoryimpl

import com.google.inject.Inject
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import domains.bot.Bot.BotName
import domains.bot.{Bot, BotRepository}
import domains.post.Post.PostId
import eu.timepit.refined.api.Refined
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import infra.dto.Tables._
import play.api.libs.ws.WSClient
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import infra.dao.slack.UsersDao
import infra.syntax.all._

import scala.concurrent.{ExecutionContext, Future, blocking}

class BotRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val ws: WSClient,
  protected val usersDao: UsersDao
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile] with BotRepository
    with API {
  override def find(botId: Bot.BotId): Future[Bot] = {

    val accessTokenQ =
      AccessTokens.filter(_.botId === botId.value.value).map(_.token).result

    val postQ =
      BotsPosts.filter(_.botId === botId.value.value).map(_.postId).result

    (for {
      resp <-
        usersDao.info(sys.env.getOrElse("ACCESS_TOKEN", ""), botId.value.value)
    } yield db.run {
      for {
        accessToken <- accessTokenQ
        postId      <- postQ
      } yield Bot(
        botId,
        BotName(Refined.unsafeApply(resp.name)),
        accessToken.map(at =>
          AccessTokenPublisherToken(Refined.unsafeApply(at))
        ),
        postId.map(pid => PostId(Refined.unsafeApply(pid))),
        None,
        None
      )
    }.ifFailedThenToInfraError("error while BotRepository.find")).flatten
  }

  override def update(
    bot: Bot,
    accessToken: AccessTokenPublisherToken
  ): Future[Unit] = for (
    _ <- db.run {
           AccessTokens += AccessTokensRow(
             accessToken.value.value,
             bot.id.value.value
           )
         }.ifFailedThenToInfraError("error while BotRepository.update")
  ) yield ()
}
