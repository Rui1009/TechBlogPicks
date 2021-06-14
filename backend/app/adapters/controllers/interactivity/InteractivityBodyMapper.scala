package adapters.controllers.interactivity

import adapters.controllers.helpers.JsonRequestMapper
import adapters.{AdapterError, BadRequestError}
//import adapters.controllers.interactivity.ChannelSelectActionInteractivityBody._
//import adapters.controllers.interactivity.InteractivityBody._
import cats.implicits._
import domains.DomainError
import domains.application.Application.ApplicationId
import domains.channel.Channel.ChannelId
import domains.workspace.WorkSpace.WorkSpaceId
import io.circe.generic.auto._
import play.api.mvc.{BaseController, BodyParser}

import scala.concurrent.ExecutionContext

sealed trait InteractivityBody
//object InteractivityBody {
//  implicit val decodeInteractivity: Decoder[InteractivityBody] =
//    List[Decoder[InteractivityBody]](
//      Decoder[ChannelSelectActionInteractivityBody](
//        decodeChannelSelectActionInteractivityBody
//      ).widen
//    ).reduceLeft(_ or _)
//}

final case class ChannelSelectActionWorkSpaceInfo(id: String)

final case class ChannelSelectActionBodyItem(
  selected_channel: String,
  `type`: "channels_select"
)

final case class ChannelSelectActionInteractivityBodyItem(
  api_app_id: String,
  team: ChannelSelectActionWorkSpaceInfo,
  actions: Seq[ChannelSelectActionBodyItem]
)

final case class ChannelSelectActionInteractivityBody(
  payload: Seq[ChannelSelectActionInteractivityBodyItem]
) extends InteractivityBody

//object ChannelSelectActionInteractivityBody {
//  implicit val decodeChannelSelectActionInteractivityBody
//    : Decoder[Seq[ChannelSelectActionInteractivityBody]] =
//    deriveDecoder[Seq[ChannelSelectActionInteractivityBody]].prepare(body =>
//      body
//    )
//}

sealed trait InteractivityCommand
final case class ChannelSelectActionInteractivityCommand(
  channelId: ChannelId,
  applicationId: ApplicationId,
  workSpaceId: WorkSpaceId
) extends InteractivityCommand
object ChannelSelectActionInteractivityCommand {
  def validate(
    body: ChannelSelectActionInteractivityBody
  ): Either[BadRequestError, InteractivityCommand] = for {
    channelSelectActionItem <-
      body.payload.headOption
        .toRight(BadRequestError("empty select action interactivity body item"))
    bodyitem                <- channelSelectActionItem.actions.headOption
                                 .toRight(BadRequestError("empty select action body item"))
    command                 <-
      (
        ChannelId.create(bodyitem.selected_channel).toValidatedNec,
        ApplicationId.create(channelSelectActionItem.api_app_id).toValidatedNec,
        WorkSpaceId.create(channelSelectActionItem.team.id).toValidatedNec
      ).mapN(ChannelSelectActionInteractivityCommand.apply)
        .toEither
        .leftMap { error =>
          BadRequestError(
            error
              .foldLeft("")((acc, cur: DomainError) => acc + cur.errorMessage)
          )
        }
  } yield command
}

trait InteractivityBodyMapper extends JsonRequestMapper {
  this: BaseController =>
  def mapToInteractivityCommand(implicit
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, InteractivityCommand]] =
    mapToValueObject[Seq[
      ChannelSelectActionInteractivityBody
    ], InteractivityCommand] { body =>
      body.headOption
        .toRight(BadRequestError("empty channel body"))
        .flatMap(ChannelSelectActionInteractivityCommand.validate)
    }
}
