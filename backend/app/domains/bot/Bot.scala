package domains.bot

import domains.EmptyStringError
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import domains.bot.Bot.{BotId, BotName}
import domains.post.Post.PostId
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import io.estatico.newtype.macros.newtype

final case class Bot(
  id: BotId,
  name: BotName,
  accessTokens: Seq[AccessTokenPublisherToken],
  posts: Seq[PostId]
) {
  def receiveToken(token: AccessTokenPublisherToken): Bot =
    this.copy(accessTokens = accessTokens :+ token)
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

}
