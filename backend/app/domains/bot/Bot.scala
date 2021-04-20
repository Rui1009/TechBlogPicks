package domains.bot

import domains.EmptyStringError
import domains.workspace.WorkSpace.WorkSpaceToken
import domains.bot.Bot.{
  BotChannelId,
  BotClientId,
  BotClientSecret,
  BotId,
  BotName
}
import domains.post.Post.PostId
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import io.estatico.newtype.macros.newtype

final case class Bot(
  id: BotId,
  name: BotName,
  accessTokens: Seq[WorkSpaceToken],
  posts: Seq[PostId],
  channels: Seq[BotChannelId],
  clientId: Option[BotClientId],
  clientSecret: Option[BotClientSecret]
) {
  def updateClientInfo(
    clientId: Option[BotClientId],
    clientSecret: Option[BotClientSecret]
  ): Bot = this.copy(clientId = clientId, clientSecret = clientSecret)

  def joinedBot(channelId: BotChannelId): Bot =
    this.copy(channels = channels :+ channelId)
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

  @newtype case class BotClientId(value: String Refined NonEmpty)
  object BotClientId {
    def create(value: String): Either[EmptyStringError, BotClientId] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("BotClientId"))
        case Right(v) => Right(BotClientId(v))
      }
  }

  @newtype case class BotClientSecret(value: String Refined NonEmpty)
  object BotClientSecret {
    def create(value: String): Either[EmptyStringError, BotClientSecret] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("BotClientSecret"))
        case Right(v) => Right(BotClientSecret(v))
      }
  }

  @newtype case class BotChannelId(value: String Refined NonEmpty)
  object BotChannelId {
    def create(value: String): Either[EmptyStringError, BotChannelId] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("BotChannelId"))
        case Right(v) => Right(BotChannelId(v))
      }
  }
}
