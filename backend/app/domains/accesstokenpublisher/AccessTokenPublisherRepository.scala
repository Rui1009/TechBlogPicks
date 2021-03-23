package domains.accesstokenpublisher

import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherTemporaryOauthCode
import domains.bot.Bot.{BotClientId, BotClientSecret}

import scala.concurrent.Future

trait AccessTokenPublisherRepository {
  def find(
    code: AccessTokenPublisherTemporaryOauthCode,
    clientId: BotClientId,
    clientSecret: BotClientSecret
  ): Future[Option[AccessTokenPublisher]]
}
