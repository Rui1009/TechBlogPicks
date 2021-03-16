package infra.format

import domains.accesstokenpublisher.AccessTokenPublisher
import io.circe.Decoder

trait AccessTokenPublisherDecoder {
  implicit val decodeAccessTokenPublisher: Decoder[AccessTokenPublisher] = Decoder.instance { cursor =>
    for {
      accessTokenPublisherToken <- cursor.downField("access_token").as[String]
    } yield AccessTokenPublisher()

  }
}
