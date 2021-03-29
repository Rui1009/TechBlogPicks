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

import scala.concurrent.ExecutionContext

final case class EventRootBody(
  teamId: Option[String],
  apiAppId: Option[String],
  `type`: String,
  event: Option[EventBody],
  challenge: Option[String]
)
object EventRootBody {
  implicit val bodyDecoder: Decoder[EventRootBody] =
    Decoder.forProduct5("team_id", "api_app_id", "type", "event", "challenge")(
      (teamId, apiAppId, _type, event, challenge) =>
        EventRootBody(teamId, apiAppId, _type, event, challenge)
    )
}

final case class EventBody(`type`: String)

final case class EventRootCommand(
  teamId: Option[WorkSpaceId],
  apiAppId: Option[BotId],
  eventType: String,
  event: Option[EventCommand],
  challenge: Option[String]
)

final case class EventCommand(eventType: String)

trait EventBodyMapper extends JsonRequestMapper { this: BaseController =>
  def mapToEventCommand(implicit
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, EventRootCommand]] =
    mapToValueObject[EventRootBody, EventRootCommand] { body =>
      (
        body.teamId.traverse(id => WorkSpaceId.create(id).toValidatedNec),
        body.apiAppId.traverse(id => BotId.create(id).toValidatedNec)
      ).mapN((teamId, apiAppId) =>
        EventRootCommand(
          teamId,
          apiAppId,
          body.`type`,
          body.event.map(e => EventCommand(e.`type`)),
          body.challenge
        )
      ).toEither
        .leftMap(errors =>
          BadRequestError(
            errors
              .foldLeft("")((acc, curr: DomainError) => acc + curr.errorMessage)
          )
        )
    }
}
