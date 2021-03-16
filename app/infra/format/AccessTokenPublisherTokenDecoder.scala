package infra.format

import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import eu.timepit.refined.api.Refined
import io.circe.Decoder
import eu.timepit.refined.auto._

trait AccessTokenPublisherTokenDecoder {
  implicit val decodeAccessTokenPublisherToken
    : Decoder[AccessTokenPublisherToken] = Decoder.instance { cursor =>
    for {
      accessTokenPublisherToken <- cursor.downField("access_token").as[String]
    } yield AccessTokenPublisherToken(Refined.unsafeApply(accessTokenPublisherToken))

  }
}
