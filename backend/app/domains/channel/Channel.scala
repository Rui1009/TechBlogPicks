package domains.channel

import domains.{EmptyStringError, RegexError}
import domains.channel.Channel.ChannelId
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import eu.timepit.refined.string.{Url, ValidFloat}
import io.estatico.newtype.macros.newtype
import cats.implicits._
import domains.channel.ChannelMessage.{
  ChannelMessageSenderUserId,
  ChannelMessageSentAt
}
import domains.channel.DraftMessage.{
  DraftMessageSenderUserId,
  DraftMessageSentAt,
  MessageBlock
}

final case class Channel(id: ChannelId, history: Seq[Message]) {
  def isMessageExists: Boolean = this.history.nonEmpty

  def addMessage(message: Message): Channel =
    this.copy(history = history :+ message)
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

sealed trait Message
final case class ChannelMessage(
  sentAt: ChannelMessageSentAt,
  senderUserId: ChannelMessageSenderUserId,
  text: String
) extends Message

object ChannelMessage {
  @newtype case class ChannelMessageSentAt(value: String Refined ValidFloat)
  object ChannelMessageSentAt {
    def create(value: String): Either[RegexError, ChannelMessageSentAt] =
      refineV[ValidFloat](value) match {
        case Right(v) => Right(ChannelMessageSentAt(v))
        case Left(_)  => Left(RegexError("ChannelMessageSentAt"))
      }
  }

  @newtype case class ChannelMessageSenderUserId(value: String Refined NonEmpty)
  object ChannelMessageSenderUserId {
    def create(
      value: String
    ): Either[EmptyStringError, ChannelMessageSenderUserId] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(ChannelMessageSenderUserId(v))
        case Left(_)  => Left(EmptyStringError("ChannelMessageSenderUserId"))
      }
  }
}

final case class DraftMessage(
  sentAt: DraftMessageSentAt,
  senderUserId: DraftMessageSenderUserId,
  blocks: Seq[MessageBlock]
) extends Message

object DraftMessage {
  @newtype case class DraftMessageSentAt(value: String Refined ValidFloat)
  object DraftMessageSentAt {
    def create(value: String): Either[RegexError, DraftMessageSentAt] =
      refineV[ValidFloat](value) match {
        case Right(v) => Right(DraftMessageSentAt(v))
        case Left(_)  => Left(RegexError("DraftMessageSentAt"))
      }
  }

  @newtype case class DraftMessageSenderUserId(value: String Refined NonEmpty)
  object DraftMessageSenderUserId {
    def create(
      value: String
    ): Either[EmptyStringError, DraftMessageSenderUserId] =
      refineV[NonEmpty](value) match {
        case Right(v) => Right(DraftMessageSenderUserId(v))
        case Left(_)  => Left(EmptyStringError("DraftMessageSenderUserId"))
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
