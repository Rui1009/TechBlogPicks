package infra.repositoryimpl

import com.google.inject.Inject
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import domains.bot.Bot.BotName
import domains.bot.{Bot, BotRepository}
import domains.post.Post.PostId
import eu.timepit.refined.api.Refined
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile
import infra.dto.Tables._
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import io.circe.parser._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import infra.dao.slack.{UsersDao, UsersDaoImpl}
import eu.timepit.refined.auto._

import scala.concurrent.{ExecutionContext, Future}

class BotRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val ws: WSClient,
  protected val usersDao: UsersDao
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile] with BotRepository
    with API {
  override def find(botId: Bot.BotId): Future[Bot] = {
    //: Todo 環境変数化する
    val winkieWorkSpaceOauthToken =
      "xoxb-1857273131876-1879915905377-DQEJFucCGmsNr9LLswBskuXC"

    (for {
      resp <- usersDao.info(winkieWorkSpaceOauthToken, botId.value.value)
    } yield for {
      accessToken <- db.run {
                                    AccessTokens
                                      .filter(_.botId === botId.value.value)
                                      .map(_.token)
                                      .result
                                  }
      postIds <- db.run {
                                    BotsPosts
                                      .filter(_.botId === botId.value.value)
                                      .map(_.postId)
                                      .result
                                  }
    } yield Bot(
      botId,
      BotName(Refined.unsafeApply(resp.name)),
      accessToken.map(at => AccessTokenPublisherToken(Refined.unsafeApply(at))),
      postIds.map(pid => PostId(Refined.unsafeApply(pid)))
    )).flatten
  }
  override def update(bot: Bot): Future[Unit] = ???
}
