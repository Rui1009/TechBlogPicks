package domains.accesstokenpublisher

import domains.EmptyStringError
import domains.accesstokenpublisher.AccessTokenPublisher.{
  AccessTokenPublisherTemporaryOauthCode,
  AccessTokenPublisherToken
}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import io.estatico.newtype.macros.newtype

final case class AccessTokenPublisher(
  token: AccessTokenPublisherToken,
  temporaryOauthCode: AccessTokenPublisherTemporaryOauthCode
) {
  def publishToken: AccessTokenPublisherToken =
    AccessTokenPublisherToken(token.value)
}

object AccessTokenPublisher {
  @newtype case class AccessTokenPublisherToken(value: String Refined NonEmpty)
  object AccessTokenPublisherToken {
    def create(
      value: String
    ): Either[EmptyStringError, AccessTokenPublisherToken] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(AccessTokenPublisherToken(v))
        case Left(_)  => Left(EmptyStringError("Token"))
      }
  }

  @newtype case class AccessTokenPublisherTemporaryOauthCode(
    value: String Refined NonEmpty
  )
  object AccessTokenPublisherTemporaryOauthCode {
    def create(
      value: String
    ): Either[EmptyStringError, AccessTokenPublisherTemporaryOauthCode] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(AccessTokenPublisherTemporaryOauthCode(v))
        case Left(_)  => Left(EmptyStringError("temporaryOauthCode"))
      }
  }
}
