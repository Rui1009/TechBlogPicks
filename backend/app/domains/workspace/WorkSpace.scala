package domains.workspace

import domains.EmptyStringError
import domains.bot.Bot
import domains.bot.Bot.BotId
import domains.workspace.WorkSpace._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import io.estatico.newtype.macros.newtype

final case class WorkSpace(
  id: WorkSpaceId,
  token: WorkSpaceToken,
  temporaryOauthCode: WorkSpaceTemporaryOauthCode,
  botIds: Seq[BotId]
) {
  def publishToken: WorkSpaceToken = WorkSpaceToken(token.value)

  def installBot(bot: Bot): WorkSpace = ???
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

  @newtype case class WorkSpaceToken(value: String Refined NonEmpty)
  object WorkSpaceToken {
    def create(value: String): Either[EmptyStringError, WorkSpaceToken] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(WorkSpaceToken(v))
        case Left(_)  => Left(EmptyStringError("Token"))
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
