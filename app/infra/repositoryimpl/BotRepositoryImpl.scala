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
import play.api.libs.ws._
import io.circe.parser._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import eu.timepit.refined.auto._
import infra.format.BotNameDecoder
import infra.syntax.all._

import scala.concurrent.{ExecutionContext, Future}

class BotRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val ws: WSClient
) (implicit val ec: ExecutionContext) extends HasDatabaseConfigProvider[PostgresProfile] with BotRepository with API with BotNameDecoder {
  override def find(botId: Bot.BotId): Future[Option[Bot]]= {
    val getUserURL = "https://slack.com/api/users.info"
    //: Todo 環境変数化する
    val winkieWorkSpaceOauthToken = "xoxb-1857273131876-1879915905377-zb373h8oddEt7TqKuL022O76"

    (for {
      resp <- ws.url(getUserURL).withHttpHeaders("token" -> winkieWorkSpaceOauthToken, "user" -> botId.value.value).get // not found & 通信のハンドリング
    } yield for {
      accessToken: Seq[String] <- db.run {
        AccessTokens.filter(_.botId === botId.value.value).map(_.token).result
    }
      postIds: Seq[Long] <- db.run {
        BotsPosts.filter(_.botId === botId.value.value).map(_.postId).result
      }
  }  yield for {
      botName <- decode[BotName](resp.json.toString).ifLeftThenReturnNone
    } yield Bot(botId, botName, accessToken.map(at => AccessTokenPublisherToken(Refined.unsafeApply(at))), postIds.map(pid => PostId(Refined.unsafeApply(pid))))).flatten
  }
  override def update(bot: Bot): Future[Unit] = ???
  }
