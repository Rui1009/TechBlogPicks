package infra.format

import domains.workspace.WorkSpace.WorkSpaceToken
import eu.timepit.refined.api.Refined
import io.circe.Decoder

trait AccessTokenPublisherTokenDecoder {
  implicit val decodeAccessTokenPublisherToken: Decoder[WorkSpaceToken] =
    Decoder.instance { cursor =>
      for {
        accessTokenPublisherToken <- cursor.downField("access_token").as[String]
      } yield WorkSpaceToken(Refined.unsafeApply(accessTokenPublisherToken))

    }
}
