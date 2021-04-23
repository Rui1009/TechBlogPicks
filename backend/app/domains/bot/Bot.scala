package domains.bot

import domains.EmptyStringError
import domains.application.Application.ApplicationId
import domains.bot.Bot.{BotAccessToken, BotId, BotName}
import domains.channel.{Channel, Message}
import domains.channel.Channel.ChannelId
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import io.estatico.newtype.macros.newtype

final case class Bot(
  id: Option[BotId],
  name: BotName,
  applicationId: ApplicationId,
  accessToken: Option[BotAccessToken],
  channelIds: Seq[ChannelId]
) {
  def joinTo(channelId: ChannelId): Bot =
    this.copy(channelIds = channelIds :+ channelId)

  def postMessage(channel: Channel, message: Message): Channel =
    channel.addMessage(message)
}

object Bot {
  @newtype case class BotId(value: String Refined NonEmpty)
  object BotId {
    def create(value: String): Either[EmptyStringError, BotId] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("BotId"))
        case Right(v) => Right(BotId(v))
      }
  }

  @newtype case class BotName(value: String Refined NonEmpty)
  object BotName {
    def create(value: String): Either[EmptyStringError, BotName] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("BotName"))
        case Right(v) => Right(BotName(v))
      }
  }

  @newtype case class BotAccessToken(value: String Refined NonEmpty)
  object BotAccessToken {
    def create(value: String): Either[EmptyStringError, BotAccessToken] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(BotAccessToken(v))
        case Left(_)  => Left(EmptyStringError("BotAccessToken"))
      }
  }
}
