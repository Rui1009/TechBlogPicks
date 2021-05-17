package adapters.controllers.interactivity

import adapters.{AdapterError, BadRequestError}
import adapters.controllers.helpers.JsonRequestMapper
//import adapters.controllers.interactivity.ChannelSelectActionInteractivityBody._
//import adapters.controllers.interactivity.InteractivityBody._
import domains.channel.Channel.ChannelId
import io.circe.{Decoder, Json}
import cats.implicits._
import domains.application.Application.ApplicationId
import domains.workspace.WorkSpace.WorkSpaceId
import domains.{DomainError, EmptyStringError}
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.auto._
import play.api.mvc.{BaseController, BodyParser}

import scala.collection.immutable.ListMap
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
  ): Either[BadRequestError, InteractivityCommand] = (
    ChannelId
      .create(body.payload.head.actions.head.selected_channel)
      .toValidatedNec,
    ApplicationId.create(body.payload.head.api_app_id).toValidatedNec,
    WorkSpaceId.create(body.payload.head.team.id).toValidatedNec
  ).mapN(ChannelSelectActionInteractivityCommand.apply)
    .toEither
    .leftMap(error =>
      BadRequestError(
        error.foldLeft("")((acc, cur: DomainError) => acc + cur.errorMessage)
      )
    )
}

trait InteractivityBodyMapper extends JsonRequestMapper {
  this: BaseController =>
  def mapToInteractivityCommand(implicit
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, InteractivityCommand]] =
    mapToValueObject[Seq[
      ChannelSelectActionInteractivityBody
    ], InteractivityCommand] { body =>
      ChannelSelectActionInteractivityCommand.validate(body.head)
    }
}
