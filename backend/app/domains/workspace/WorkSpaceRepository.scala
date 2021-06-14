package domains.workspace

import domains.application.Application._
import domains.channel.Channel.ChannelId
import domains.workspace.WorkSpace._

import scala.concurrent.Future

trait WorkSpaceRepository {
  def find(
    code: WorkSpaceTemporaryOauthCode,
    clientId: ApplicationClientId,
    clientSecret: ApplicationClientSecret
  ): Future[WorkSpace]

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
  def findByConstToken(
    id: WorkSpaceId
  ): Future[
    Option[WorkSpace]
  ] // 正確なWorkSpaceが返らないのでなくしたい。現状はUninstallApplicationUseCaseのみ
  def sendMessage(
    workSpace: WorkSpace,
    applicationId: ApplicationId,
    channelId: ChannelId
  ): Future[Option[Unit]]
}
