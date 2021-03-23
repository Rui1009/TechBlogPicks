package domains.bot

import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import domains.bot.Bot.BotId

import scala.concurrent.Future

trait BotRepository {
  def find(botId: BotId): Future[Bot]
  def update(bot: Bot, accessToken: AccessTokenPublisherToken): Future[Unit]
  def update(bot: Bot): Future[Unit]
}
