package infra.repositoryimpl

import com.google.inject.Inject
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import domains.bot.Bot.BotName
import domains.bot.{Bot, BotRepository}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile
import infra.dto.Tables._
import play.api.libs.json.Json
import play.api.libs.ws._
import io.circe.parser._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import eu.timepit.refined.auto._

import scala.concurrent.{ExecutionContext, Future}

class BotRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val ws: WSClient
) (implicit val ec: ExecutionContext) extends HasDatabaseConfigProvider[PostgresProfile] with BotRepository with API {
  override def find(botId: Bot.BotId): Future[Bot] = {
    val getUserURL = "https://slack.com/api/users.info"

    for {
      resp <- ws.url(getUserURL).withHttpHeaders("token" -> botId.value.value).get // not found & 通信のハンドリング
    } yield for {
      botName <- decode[BotName](resp.json.toString)
    } yield for {
      accessToken: Seq[String] <- db.run {
        AccessTokens.filter(_.botId === botId.value.value).map(_.token).result
    }
  } yield accessToken.map(AccessTokenPublisherToken(_))

}
  override def update(bot: Bot): Future[Unit] = ???
  }
