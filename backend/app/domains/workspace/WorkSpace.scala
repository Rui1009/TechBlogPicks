package domains.workspace

import domains._
import domains.application.Application
import domains.application.Application.ApplicationId
import domains.bot.Bot
import domains.bot.Bot._
import domains.channel.{Channel, DraftMessage}
import domains.channel.Channel.ChannelId
import domains.workspace.WorkSpace._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import io.estatico.newtype.macros.newtype
import cats.implicits._

final case class WorkSpace(
  id: WorkSpaceId,
  temporaryOauthCode: Option[WorkSpaceTemporaryOauthCode],
  bots: Seq[Bot],
  channels: Seq[Channel],
  unallocatedToken: Option[BotAccessToken]
) {
  def installApplication(
    application: Application
  ): Either[DomainError, WorkSpace] = this.unallocatedToken match {
    case Some(token) =>
      this.bots.find(bo => bo.applicationId == application.id) match {
        case Some(_) => Left(DuplicateError("bots"))
        case None    =>
          val bot = Bot(
            None,
            BotName(application.name.value),
            application.id,
            token,
            Seq(),
            None
          )
          Right(this.copy(bots = bots :+ bot))
      }
    case None        => Left(NotExistError("unallocatedToken"))
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
  ): Either[DomainError, WorkSpace] = findBotByApplicationId(appId).map { bot =>
    val joinedBot = bot.joinTo(channelId)
    this.copy(bots = this.bots.filter(_.id != joinedBot.id) :+ joinedBot)
  }

  private def findBotByApplicationId(
    appId: ApplicationId
  ): Either[DomainError, Bot] = this.bots.find(_.applicationId == appId) match {
    case Some(v) => Right(v)
    case None    => Left(NotExistError("ApplicationId"))
  }

  def findChannel(channelId: ChannelId): Either[DomainError, Channel] =
    this.channels.find(_.id == channelId) match {
      case Some(v) => Right(v)
      case None    => Left(NotExistError("ChannelId"))
    }

  def botCreateOnboardingMessage(
    applicationId: ApplicationId
  ): Either[DomainError, WorkSpace] =
    this.bots.find(bot => bot.applicationId == applicationId) match {
      case Some(v) => Right(
          this.copy(bots =
            bots.filter(bot => bot.id != v.id) :+ v.createOnboardingMessage
          )
        )
      case None    => Left(NotExistError("Bot"))
    }

  def botCreateGreetingInInvitedChannel(
    botId: BotId
  ): Either[DomainError, WorkSpace] =
    this.bots.find(bot => bot.id.contains(botId)) match {
      case Some(v) => Right(
          this.copy(bots =
            bots
              .filter(bot => bot.id != v.id) :+ v.createGreetingInInvitedChannel
          )
        )
      case None    => Left(NotExistError("Bot"))
    }

  def botPostMessage(
    applicationId: ApplicationId,
    channelId: ChannelId
  ): Either[DomainError, WorkSpace] = for {
    targetBot <- this.bots
                   .find(bot => bot.applicationId == applicationId)
                   .toRight(NotExistError("Bot"))

    targetChannel  <- this.channels
                        .find(channel => channel.id == channelId)
                        .toRight(NotExistError("ChannelId"))
    updatedChannel <- targetBot.postMessage(targetChannel)
  } yield this.copy(channels = channels.filter(_.id != targetChannel.id) :+ updatedChannel)
}

object WorkSpace {
  @newtype case class WorkSpaceId(value: String Refined NonEmpty)
  object WorkSpaceId extends VOFactory[EmptyStringError] {
    override def castError(e: ValidationError): EmptyStringError =
      EmptyStringError("WorkSpaceId")
  }

  @newtype case class WorkSpaceTemporaryOauthCode(
    value: String Refined NonEmpty
  )
  object WorkSpaceTemporaryOauthCode extends VOFactory[EmptyStringError] {
    override def castError(e: ValidationError): EmptyStringError =
      EmptyStringError("WorkSpaceTemporaryOauthCode")
  }
}
