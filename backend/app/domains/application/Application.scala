package domains.application

import domains.application.Application.{
  ApplicationClientId,
  ApplicationClientSecret,
  ApplicationId,
  ApplicationName
}
import domains.post.Post.PostId
import domains.{EmptyStringError, VOFactory, ValidationError}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
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
  object ApplicationId extends VOFactory[EmptyStringError] {
    override def castError(e: ValidationError): EmptyStringError =
      EmptyStringError("ApplicationId")
  }

  @newtype case class ApplicationName(value: String Refined NonEmpty)
  object ApplicationName extends VOFactory[EmptyStringError] {
    override def castError(e: ValidationError): EmptyStringError =
      EmptyStringError("ApplicationName")
  }

  @newtype case class ApplicationClientId(value: String Refined NonEmpty)
  object ApplicationClientId extends VOFactory[EmptyStringError] {
    override def castError(e: ValidationError): EmptyStringError =
      EmptyStringError("ApplicationClientId")
  }

  @newtype case class ApplicationClientSecret(value: String Refined NonEmpty)
  object ApplicationClientSecret extends VOFactory[EmptyStringError] {
    override def castError(e: ValidationError): EmptyStringError =
      EmptyStringError("ApplicationClientSecret")
  }
}
