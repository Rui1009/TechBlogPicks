package domains.bot

import domains.workspace.WorkSpace.{WorkSpaceId, WorkSpaceToken}
import domains.bot.Bot.BotId

import scala.concurrent.Future

trait BotRepository {
  def find(botId: BotId): Future[Option[Bot]]
  def find(botId: BotId, workspaceId: WorkSpaceId): Future[Option[Bot]]
  def update(bot: Bot): Future[Unit]
  def update(accessToken: WorkSpaceToken): Future[Unit]
  def join(joinedBot: Bot): Future[Unit]
}
