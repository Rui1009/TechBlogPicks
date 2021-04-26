package domains.workspace

import domains.application.Application.{
  ApplicationClientId,
  ApplicationClientSecret,
  ApplicationId
}
import domains.workspace.WorkSpace.{WorkSpaceId, WorkSpaceTemporaryOauthCode}
import domains.bot.Bot.BotId

import scala.concurrent.Future

trait WorkSpaceRepository {
  def find(
    code: WorkSpaceTemporaryOauthCode,
    clientId: ApplicationClientId,
    clientSecret: ApplicationClientSecret
  ): Future[Option[WorkSpace]]

  def add(model: WorkSpace, applicationId: ApplicationId): Future[Unit]
  def update(model: WorkSpace): Future[Unit]
  def find(id: WorkSpaceId): Future[Option[WorkSpace]]
}
