package domains.bot

import domains.{EmptyStringError, NotExistError}
import domains.application.Application.ApplicationId
import domains.bot.Bot.{BotAccessToken, BotId, BotName}
import domains.channel.{Channel, DraftMessage}
import domains.channel.Channel.ChannelId
import domains.channel.DraftMessage.{
  ActionBlock,
  ActionSelect,
  BlockText,
  SectionBlock,
  SelectPlaceHolder
}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import eu.timepit.refined.auto._
import io.estatico.newtype.macros.newtype

final case class Bot(
  id: Option[BotId],
  name: BotName,
  applicationId: ApplicationId,
  accessToken: BotAccessToken,
  channelIds: Seq[ChannelId],
  draftMessage: Option[DraftMessage]
) {
  def joinTo(channelId: ChannelId): Bot =
    this.copy(channelIds = channelIds.filter(_ != channelId) :+ channelId)

  def createOnboardingMessage: Bot = {
    val draft = DraftMessage(
      Seq(
        SectionBlock(
          BlockText(
            Refined.unsafeApply(
              "インストールありがとうございます🤗\nWinkieはあなたの関心のある分野に関する最新の技術記事を自動でslack上に定期配信するアプリです。\nご利用いただくために、初めにアプリを追加するチャンネルを選択してください。"
            )
          ),
          None
        ),
        ActionBlock(
          Seq(
            ActionSelect(
              "channels_select",
              SelectPlaceHolder("Select a channel", false),
              "actionId-0"
            )
          )
        )
      )
    )
    this.copy(draftMessage = Some(draft))
  }

  def createGreetingInInvitedChannel: Bot = {
    val draft = DraftMessage(
      Seq(
        SectionBlock(
          BlockText(
            Refined.unsafeApply(
              "<!channel>招待ありがとうございます!🤗\nWinkieは最新の技術記事をジャンル別で自動配信するslackアプリです。\nこれから毎日コンテンツを配信していくのでお楽しみに！"
            )
          ),
          None
        )
      )
    )
    this.copy(draftMessage = Some(draft))
  }

  def postMessage(channel: Channel): Either[NotExistError, Channel] = for {
    message <- this.draftMessage.toRight(NotExistError("DraftMessage"))
  } yield channel.receiveMessage(message)
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

  @newtype case class BotAccessToken(value: String Refined NonEmpty)
  object BotAccessToken {
    def create(value: String): Either[EmptyStringError, BotAccessToken] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(BotAccessToken(v))
        case Left(_)  => Left(EmptyStringError("BotAccessToken"))
      }
  }
}
