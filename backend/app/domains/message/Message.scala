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
  case class AccessoryImage(imageUrl: ImageUrl, imageAltText: ImageAltText)
      extends BlockAccessory
  object AccessoryImage {
    @newtype case class ImageUrl(value: String Refined Url)
    object ImageUrl {
      def create(value: String): Either[RegexError, ImageUrl] =
        refineV[Url](value) match {
          case Right(v) => Right(ImageUrl(v))
          case Left(_)  => Left(RegexError("AccessoryImageUrl"))
        }
    }
    case class ImageAltText(text: String) extends AnyVal
  }

  case class ActionBlock(actionBlockElements: Seq[ActionBlockElement])
      extends MessageBlock
  sealed trait ActionBlockElement
  case class ActionSelect(
    actionType: SelectActionType,
    placeholder: SelectPlaceHolder,
    actionId: SelectActionId
  ) extends ActionBlockElement
  object ActionSelect {
    @newtype case class SelectActionType(value: String Refined NonEmpty)
    object SelectActionType {
      def create(value: String): Either[EmptyStringError, SelectActionType] =
        refineV[NonEmpty](value) match {
          case Right(v) => Right(SelectActionType(v))
          case Left(_)  => Left(EmptyStringError("SelectActionType"))
        }
    }

    @newtype case class SelectActionId(value: String Refined NonEmpty)
    object SelectActionId {
      def create(value: String): Either[EmptyStringError, SelectActionId] =
        refineV[NonEmpty](value) match {
          case Right(v) => Right(SelectActionId(v))
          case Left(_)  => Left(EmptyStringError("SelectActionId"))
        }
    }
  }
  case class SelectPlaceHolder(
    placeHolderText: String,
    placeHolderEmoji: Boolean
  )
}
