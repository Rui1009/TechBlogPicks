package domains.workspace

import domains.application.Application._
import domains.workspace.WorkSpace._
import domains.channel.Channel.ChannelId

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
}
