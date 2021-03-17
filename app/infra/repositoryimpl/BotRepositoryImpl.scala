package infra.repositoryimpl

import com.google.inject.Inject
import domains.bot.Bot.BotName
import domains.bot.{Bot, BotRepository}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile
import infra.dto.Tables._
import play.api.libs.json.Json
import play.api.libs.ws._
import io.circe.parser._
import infra.syntax.all._

import scala.concurrent.{ExecutionContext, Future}

class BotRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val ws: WSClient
) (implicit val ec: ExecutionContext) extends HasDatabaseConfigProvider[PostgresProfile] with BotRepository {
  override def find(botId: Bot.BotId): Future[Bot] = {
    val getUserURL = "https://slack.com/api/users.info"

    for {
      resp <- ws.url(getUserURL).withHttpHeaders("token" -> botId.value.value).get // not found & 通信のハンドリング
    } yield for {
      botName <- decode[BotName](resp.json.toString)
    } yield for {
      accessToken <- db.run {
        AccessTokens.filter(_.botId == botId.value).map(_.token)
    }
  }

  override def update(bot: Bot): Future[Unit] = ???

}
