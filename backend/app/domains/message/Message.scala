package domains.message

import domains.message.Message.AccessoryImage.{ImageAltText, ImageUrl}
import domains.message.Message.ActionSelect.{SelectActionId, SelectActionType}
import domains.{EmptyStringError, RegexError}
import domains.message.Message.{
  MessageBlock,
  MessageChannelId,
  MessageId,
  MessageSentAt,
  MessageUserId,
  SelectPlaceHolder
}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.refineV
import eu.timepit.refined.string.{Url, ValidFloat}
import io.estatico.newtype.macros.newtype

final case class Message(
  id: Option[MessageId],
  sentAt: Option[MessageSentAt],
  userId: MessageUserId,
  channelId: MessageChannelId,
  blocks: Seq[MessageBlock]
)

object Message {
  @newtype case class MessageId(value: String Refined NonEmpty)
  object MessageId {
    def create(value: String): Either[EmptyStringError, MessageId] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(MessageId(v))
        case Left(_)  => Left(EmptyStringError("MessageId"))
      }
  }

  @newtype case class MessageSentAt(value: String Refined ValidFloat)
  object MessageSentAt {
    def create(value: String): Either[RegexError, MessageSentAt] =
      refineV[ValidFloat](value) match {
        case Right(v) => Right(MessageSentAt(v))
        case Left(_)  => Left(RegexError("MessageSentAt"))
      }
  }

  @newtype case class MessageUserId(value: String Refined NonEmpty)
  object MessageUserId {
    def create(value: String): Either[EmptyStringError, MessageUserId] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(MessageUserId(v))
        case Left(_)  => Left(EmptyStringError("MessageUserId"))
      }
  }

  @newtype case class MessageChannelId(value: String Refined NonEmpty)
  object MessageChannelId {
    def create(value: String): Either[EmptyStringError, MessageChannelId] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(MessageChannelId(v))
        case Left(_)  => Left(EmptyStringError("MessageChannelId"))
      }
  }

  sealed trait MessageBlock
  case class SectionBlock(
    blockText: BlockText,
    blockAccessory: Option[BlockAccessory]
  )                                  extends MessageBlock
  case class BlockText(text: String) extends AnyVal

  sealed trait BlockAccessory
  case class AccessoryImage(imageUrl: String, imageAltText: String)
      extends BlockAccessory

  case class ActionBlock(actionBlockElements: Seq[ActionBlockElement])
      extends MessageBlock
  sealed trait ActionBlockElement
  case class ActionSelect(
    actionType: String,
    placeholder: SelectPlaceHolder,
    actionId: String
  ) extends ActionBlockElement

  case class SelectPlaceHolder(
    placeHolderText: String,
    placeHolderEmoji: Boolean
  )
}
