package domains.workspace

import domains.workspace.WorkSpace.WorkSpaceTemporaryOauthCode
import domains.bot.Bot.{BotClientId, BotClientSecret}

import scala.concurrent.Future

trait WorkSpaceRepository {
  def find(
    code: WorkSpaceTemporaryOauthCode,
    clientId: BotClientId,
    clientSecret: BotClientSecret
  ): Future[Option[WorkSpace]]
}
