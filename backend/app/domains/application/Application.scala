package domains.application

import domains.{EmptyStringError, NegativeNumberError, RegexError}
import domains.application.Application.{
  ApplicationClientId,
  ApplicationClientSecret,
  ApplicationId,
  ApplicationName
}
import domains.post.Post.{PostId, PostUrl}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import eu.timepit.refined.string.Url
import io.estatico.newtype.macros.newtype

final case class Application(
  id: ApplicationId,
  name: ApplicationName,
  clientId: Option[ApplicationClientId],
  clientSecret: Option[ApplicationClientSecret],
  posts: Seq[PostId]
) {
  def updateClientInfo(
    clientId: Option[ApplicationClientId],
    clientSecret: Option[ApplicationClientSecret]
  ): Application = this.copy(clientId = clientId, clientSecret = clientSecret)
}

object Application {
  @newtype case class ApplicationId(value: String Refined NonEmpty)
  object ApplicationId {
    def create(value: String): Either[EmptyStringError, ApplicationId] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("ApplicationId"))
        case Right(v) => Right(ApplicationId(v))
      }
  }

  @newtype case class ApplicationName(value: String Refined NonEmpty)
  object ApplicationName {
    def create(value: String): Either[EmptyStringError, ApplicationName] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("ApplicationName"))
        case Right(v) => Right(ApplicationName(v))
      }
  }

  @newtype case class ApplicationClientId(value: String Refined NonEmpty)
  object ApplicationClientId {
    def create(value: String): Either[EmptyStringError, ApplicationClientId] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("ApplicationClientId"))
        case Right(v) => Right(ApplicationClientId(v))
      }
  }

  @newtype case class ApplicationClientSecret(value: String Refined NonEmpty)
  object ApplicationClientSecret {
    def create(
      value: String
    ): Either[EmptyStringError, ApplicationClientSecret] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("ApplicationClientSecret"))
        case Right(v) => Right(ApplicationClientSecret(v))
      }
  }
}
