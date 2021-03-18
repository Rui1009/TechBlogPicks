package infra.format

import domains.bot.Bot.BotName
import eu.timepit.refined.api.Refined
import io.circe.Decoder

trait BotNameDecoder {
  implicit val decodeBotName: Decoder[BotName] = Decoder.instance { cursor =>
    for {
      botName <- cursor.downField("user").downField("id").as[String]
    } yield BotName(Refined.unsafeApply(botName))
  }
}

