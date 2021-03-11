package domains.bot

import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import domains.bot.Bot.BotId

import scala.concurrent.Future

trait BotRepository {
  def update(botId: BotId, accessToken: AccessTokenPublisherToken): Future[Bot]
}
