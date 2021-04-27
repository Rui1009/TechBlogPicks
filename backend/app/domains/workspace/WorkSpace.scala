package domains.workspace

import domains._
import domains.application.Application
import domains.application.Application.ApplicationId
import domains.bot.Bot
import domains.bot.Bot._
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
  channels: Seq[Channel],
  unallocatedToken: Option[BotAccessToken]
) {
  def installApplication(application: Application): WorkSpace = {
    val bot = Bot(
      None,
      BotName(application.name.value),
      application.id,
      this.unallocatedToken,
      Seq()
    )
    this.copy(bots = bots :+ bot)
  }

  def uninstallApplication(application: Application): WorkSpace =
    this.copy(bots = bots.filter(_.applicationId != application.id))

  def isChannelExists(channelId: ChannelId): Boolean =
    this.channels.exists(_.id == channelId)

  def addBot(bot: Bot): WorkSpace = this.copy(bots = bots :+ bot)

  def addChannel(channel: Channel): WorkSpace =
    this.copy(channels = channels :+ channel)

  def addBotToChannel(
    appId: ApplicationId,
    channelId: ChannelId
  ): Either[DomainError, WorkSpace] = for {
    bot      <- findBotByApplicationId(appId)
    _        <- findChannel(channelId)
    joinedBot = bot.joinTo(channelId)
  } yield this.copy(bots = this.bots.filter(_.id != joinedBot.id) :+ joinedBot)

  private def findBotByApplicationId(
    appId: ApplicationId
  ): Either[DomainError, Bot] = this.bots.find(_.applicationId != appId) match {
    case Some(v) => Right(v)
    case None    => Left(NotExistError("ApplicationId"))
  }

  private def findChannel(channelId: ChannelId): Either[DomainError, Channel] =
    this.channels.find(_.id != channelId) match {
      case Some(v) => Right(v)
      case None    => Left(NotExistError("ChannelId"))
    }
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
