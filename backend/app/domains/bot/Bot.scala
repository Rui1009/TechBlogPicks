package domains.bot

import domains.application.Application.ApplicationId
import domains.bot.Bot.{BotAccessToken, BotId, BotName}
import domains.channel.Channel.ChannelId
import domains.channel.DraftMessage._
import domains.channel.{Channel, DraftMessage}
import domains.{EmptyStringError, NotExistError, VOFactory, ValidationError}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty
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
  object BotId extends VOFactory[EmptyStringError] {
    override def castError(e: ValidationError): EmptyStringError =
      EmptyStringError("BotId")
  }

  @newtype case class BotName(value: String Refined NonEmpty)
  object BotName extends VOFactory[EmptyStringError] {
    override def castError(e: ValidationError): EmptyStringError =
      EmptyStringError("BotName")
  }

  @newtype case class BotAccessToken(value: String Refined NonEmpty)
  object BotAccessToken extends VOFactory[EmptyStringError] {
    override def castError(e: ValidationError): EmptyStringError =
      EmptyStringError("BotAccessToken")
  }
}
