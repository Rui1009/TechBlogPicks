package domains.workspace

import domains.EmptyStringError
import domains.application.Application
import domains.bot.Bot
import domains.bot.Bot.{BotId, BotName}
import domains.channel.Channel
import domains.channel.Channel.ChannelId
import domains.workspace.WorkSpace._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import io.estatico.newtype.macros.newtype

final case class WorkSpace(
  id: WorkSpaceId,
  temporaryOauthCode: Option[WorkSpaceTemporaryOauthCode],
  bots: Seq[Bot],
  channels: Seq[Channel]
) {
  def installApplication(application: Application): WorkSpace = {
    val installedBot =
      Bot(None, BotName(application.name.value), application.id, None, Seq())
    this.copy(bots = bots :+ installedBot)
  }

  def uninstallApplication(application: Application): WorkSpace =
    this.copy(bots = bots.filter(_.applicationId != application.id))

  def isChannelExists(channelId: ChannelId): Boolean =
    this.channels.exists(_.id == channelId)

  def addBot(bot: Bot): WorkSpace = this.copy(bots = bots :+ bot)

  def addChannel(channel: Channel): WorkSpace =
    this.copy(channels = channels :+ channel)
}

object WorkSpace {
  @newtype case class WorkSpaceId(value: String Refined NonEmpty)
  object WorkSpaceId {
    def create(value: String): Either[EmptyStringError, WorkSpaceId] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(WorkSpaceId(v))
        case Left(_)  => Left(EmptyStringError("WorkSpaceId"))
      }
  }

  @newtype case class WorkSpaceTemporaryOauthCode(
    value: String Refined NonEmpty
  )
  object WorkSpaceTemporaryOauthCode {
    def create(
      value: String
    ): Either[EmptyStringError, WorkSpaceTemporaryOauthCode] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(WorkSpaceTemporaryOauthCode(v))
        case Left(_)  => Left(EmptyStringError("temporaryOauthCode"))
      }
  }
}
