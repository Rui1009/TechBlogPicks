package domains.workspace

import domains.application.Application.{
  ApplicationClientId,
  ApplicationClientSecret,
  ApplicationId
}
import domains.bot.Bot
import domains.workspace.WorkSpace.{WorkSpaceId, WorkSpaceTemporaryOauthCode}
import domains.bot.Bot.BotId
import domains.channel.{Channel, DraftMessage}

import scala.concurrent.Future

trait WorkSpaceRepository {
  def find(
    code: WorkSpaceTemporaryOauthCode,
    clientId: ApplicationClientId,
    clientSecret: ApplicationClientSecret
  ): Future[Option[WorkSpace]]

  def update(
    model: WorkSpace,
    applicationId: ApplicationId
  ): Future[Option[Unit]]
  def update(model: WorkSpace): Future[Unit]
  def find(id: WorkSpaceId): Future[Option[WorkSpace]]
  def sendMessage(
    bot: Bot,
    channel: Channel,
    message: DraftMessage
  ): Future[Unit]
}
