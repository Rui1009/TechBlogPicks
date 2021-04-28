package infra.format

import domains.bot.Bot.BotAccessToken
import eu.timepit.refined.api.Refined
import io.circe.Decoder

trait AccessTokenPublisherTokenDecoder {
  implicit val decodeAccessTokenPublisherToken: Decoder[BotAccessToken] =
    Decoder.instance { cursor =>
      for {
        accessTokenPublisherToken <- cursor.downField("access_token").as[String]
      } yield BotAccessToken(Refined.unsafeApply(accessTokenPublisherToken))

    }
}
