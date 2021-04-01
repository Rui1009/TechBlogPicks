package adapters.controllers.event

import adapters.controllers.helpers.JsonRequestMapper
import adapters.{AdapterError, BadRequestError}
import cats.implicits._
import domains.DomainError
import domains.bot.Bot.BotId
import domains.workspace.WorkSpace.WorkSpaceId
import io.circe._
import io.circe.generic.auto._
import play.api.mvc.{BaseController, BodyParser}
import adapters.controllers.event.AppUninstalledEventBody._
import adapters.controllers.event.EventBody._

import scala.concurrent.ExecutionContext

sealed trait EventBody
object EventBody {
  implicit val decodeEvent: Decoder[EventBody] = List[Decoder[EventBody]](
    Decoder[AppUninstalledEventBody](decodeAppUninstalledEventBody).widen,
    Decoder[UrlVerificationEventBody].widen
  ).reduceLeft(_ or _)
}

sealed trait EventCommand

final case class AppUninstalledEventBody(teamId: String, apiAppId: String)
    extends EventBody

object AppUninstalledEventBody {
  implicit val decodeAppUninstalledEventBody: Decoder[AppUninstalledEventBody] =
    Decoder.instance { cursor =>
      for {
        teamId   <- cursor.downField("team_id").as[String]
        apiAppId <- cursor.downField("api_app_id").as[String]
      } yield AppUninstalledEventBody(teamId, apiAppId)
    }
}

final case class AppUninstalledEventCommand(
  workSpaceId: WorkSpaceId,
  botId: BotId
) extends EventCommand
object AppUninstalledEventCommand {
  def validate(
    body: AppUninstalledEventBody
  ): Either[BadRequestError, EventCommand] = (
    WorkSpaceId.create(body.teamId).toValidatedNec,
    BotId.create(body.apiAppId).toValidatedNec
  ).mapN(AppUninstalledEventCommand.apply)
    .toEither
    .leftMap(errors =>
      BadRequestError(
        errors.foldLeft("")((acc, curr: DomainError) => acc + curr.errorMessage)
      )
    )
}

final case class UrlVerificationEventBody(challenge: String) extends EventBody
final case class UrlVerificationEventCommand(challenge: String)
    extends EventCommand

trait EventBodyMapper extends JsonRequestMapper { this: BaseController =>
  def mapToEventCommand(implicit
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, EventCommand]] =
    mapToValueObject[EventBody, EventCommand] {
      case body: AppUninstalledEventBody  =>
        AppUninstalledEventCommand.validate(body)
      case body: UrlVerificationEventBody =>
        Right(UrlVerificationEventCommand(body.challenge))
    }(decodeEvent, ec)
}
