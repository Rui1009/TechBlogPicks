package domains.workspace

import domains.application.Application._
import domains.workspace.WorkSpace._
import domains.channel.Channel.ChannelId
import domains.bot.Bot
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

  def joinChannels(
    model: WorkSpace,
    applicationId: ApplicationId,
    channelIds: Seq[ChannelId]
  ): Future[Unit]

  def removeBot(model: WorkSpace): Future[Unit]

  def find(id: WorkSpaceId): Future[Option[WorkSpace]]
  def sendMessage(
    workSpace: WorkSpace,
    applicationId: ApplicationId,
    channelId: ChannelId
  ): Future[Option[Unit]]
}
