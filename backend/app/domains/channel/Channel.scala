package domains.channel

import domains.{EmptyStringError, RegexError}
import domains.channel.Channel.ChannelId
import domains.channel.Message.{MessageBlock, MessageSentAt, SenderUserId}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import eu.timepit.refined.string.{Url, ValidFloat}
import io.estatico.newtype.macros.newtype
import cats.implicits._

final case class Channel(id: ChannelId, messages: Seq[Message]) {
  def isMessageExists: Boolean = this.messages.nonEmpty

  def addMessage(message: Message): Channel =
    this.copy(messages = messages :+ message)
}

object Channel {
  @newtype case class ChannelId(value: String Refined NonEmpty)
  object ChannelId {
    def create(value: String): Either[EmptyStringError, ChannelId] =
      refineV[NonEmpty](value) match {
        case Left(_)  => Left(EmptyStringError("ChannelId"))
        case Right(v) => Right(ChannelId(v))
      }
  }
}

final case class Message(
  sentAt: MessageSentAt,
  senderUserId: SenderUserId,
  blocks: Seq[MessageBlock]
) {}

object Message {
  @newtype case class MessageSentAt(value: String Refined ValidFloat)
  object MessageSentAt {
    def create(value: String): Either[RegexError, MessageSentAt] =
      refineV[ValidFloat](value) match {
        case Right(v) => Right(MessageSentAt(v))
        case Left(_)  => Left(RegexError("MessageSentAt"))
      }
  }

  @newtype case class SenderUserId(value: String Refined NonEmpty)
  object SenderUserId {
    def create(value: String): Either[EmptyStringError, SenderUserId] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(SenderUserId(v))
        case Left(_)  => Left(EmptyStringError("SenderUserId"))
      }
  }

  sealed trait MessageBlock
  case class SectionBlock(
    blockText: BlockText,
    blockAccessory: Option[BlockAccessory]
  ) extends MessageBlock
  case class BlockText(text: String Refined NonEmpty)
  object BlockText {
    def create(text: String): Either[EmptyStringError, BlockText] =
      refineV[NonEmpty](text) match {
        case Right(v) => Right(BlockText(v))
        case Left(_)  => Left(EmptyStringError("BlockText"))
      }
  }

  sealed trait BlockAccessory
  case class AccessoryImage(imageUrl: String Refined Url, imageAltText: String)
      extends BlockAccessory
  object AccessoryImage {
    def create(
      imageUrl: String,
      imageAltText: String
    ): Either[RegexError, AccessoryImage] = refineV[Url](imageUrl) match {
      case Right(v) => Right(AccessoryImage(v, imageAltText))
      case Left(_)  => Left(RegexError("ImageUrl"))
    }
  }

  case class ActionBlock(actionBlockElements: Seq[ActionBlockElement])
      extends MessageBlock
  sealed trait ActionBlockElement

  case class ActionSelect(
    actionType: String Refined NonEmpty,
    placeholder: SelectPlaceHolder,
    actionId: String Refined NonEmpty
  ) extends ActionBlockElement
  object ActionSelect {
    def create(
      actionType: String,
      placeholder: SelectPlaceHolder,
      actionId: String
    ): Either[EmptyStringError, ActionSelect] = (
      refineV[NonEmpty](actionType).left
        .map(_ => EmptyStringError("actionType"))
        .toValidatedNec,
      refineV[NonEmpty](actionId).left
        .map(_ => EmptyStringError("actionId"))
        .toValidatedNec
    ).mapN(ActionSelect.apply(_, placeholder, _))
      .toEither
      .leftMap(errors =>
        EmptyStringError(
          errors.foldLeft("")((acc, curr) => acc + "," + curr.className).drop(1)
        )
      )
  }

  case class SelectPlaceHolder(
    placeHolderText: String,
    placeHolderEmoji: Boolean
  )
}
