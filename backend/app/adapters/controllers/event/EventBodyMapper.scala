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
import adapters.controllers.event.AppHomeOpenedEventBody._
import adapters.controllers.event.EventBody._
import domains.message.Message.MessageChannelId
import play.api.Logger

import scala.concurrent.ExecutionContext

sealed trait EventBody
object EventBody {
  implicit val decodeEvent: Decoder[EventBody] = List[Decoder[EventBody]](
    Decoder[AppUninstalledEventBody](decodeAppUninstalledEventBody).widen,
    Decoder[UrlVerificationEventBody].widen,
    Decoder[AppHomeOpenedEventBody](decodeAppHomeOpenedEventBody).widen
  ).reduceLeft(_ or _)
}

sealed trait EventCommand

final case class AppUninstalledEventBody(
  teamId: String,
  apiAppId: String,
  eventType: "app_uninstalled"
) extends EventBody

object AppUninstalledEventBody {
  implicit val decodeAppUninstalledEventBody: Decoder[AppUninstalledEventBody] =
    Decoder.instance { cursor =>
      for {
        teamId    <- cursor.downField("team_id").as[String]
        appId     <- cursor.downField("api_app_id").as[String]
        eventType <-
          cursor.downField("event").downField("type").as["app_uninstalled"]
      } yield AppUninstalledEventBody(teamId, appId, eventType)
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

final case class AppHomeOpenedEventBody(
  channel: String,
  appId: String,
  teamId: String,
  eventType: "app_home_opened"
) extends EventBody
object AppHomeOpenedEventBody {
  implicit val decodeAppHomeOpenedEventBody: Decoder[AppHomeOpenedEventBody] =
    Decoder.instance { cursor =>
      for {
        channel   <- cursor.downField("event").downField("channel").as[String]
        appId     <- cursor.downField("api_app_id").as[String]
        teamId    <- cursor.downField("team_id").as[String]
        eventType <-
          cursor.downField("event").downField("type").as["app_home_opened"]
      } yield AppHomeOpenedEventBody(channel, appId, teamId, eventType)
    }
}

final case class AppHomeOpenedEventCommand(
  channelId: MessageChannelId,
  botId: BotId,
  workSpaceId: WorkSpaceId
) extends EventCommand
object AppHomeOpenedEventCommand {
  private lazy val logger                  = Logger(this.getClass)
  def validate(
    body: AppHomeOpenedEventBody
  ): Either[BadRequestError, EventCommand] = (
    MessageChannelId.create(body.channel).toValidatedNec,
    BotId.create(body.appId).toValidatedNec,
    WorkSpaceId.create(body.teamId).toValidatedNec
  ).mapN(AppHomeOpenedEventCommand.apply).toEither.leftMap { errors =>
    logger.warn(
      errors.foldLeft("")((acc, curr: DomainError) => acc + curr.errorMessage)
    )
    BadRequestError(
      errors.foldLeft("")((acc, curr: DomainError) => acc + curr.errorMessage)
    )
  }
}

final case class UrlVerificationEventBody(challenge: String) extends EventBody
final case class UrlVerificationEventCommand(challenge: String)
    extends EventCommand

trait EventBodyMapper extends JsonRequestMapper { this: BaseController =>
  private lazy val logger                           = Logger(this.getClass)
  def mapToEventCommand(implicit
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, EventCommand]] =
    mapToValueObject[EventBody, EventCommand] {
      case body: AppUninstalledEventBody =>
        logger.warn("failed case match")
        AppUninstalledEventCommand.validate(body)

      case body: UrlVerificationEventBody =>
        Right(UrlVerificationEventCommand(body.challenge))
      case body: AppHomeOpenedEventBody   =>
        logger.warn("success case match")
        AppHomeOpenedEventCommand.validate(body)
    }(decodeEvent, ec)
}
