package adapters.controllers.bot

import adapters.{AdapterError, BadRequestError}
import adapters.controllers.helpers.JsonRequestMapper
import domains.workspace.WorkSpace.{WorkSpaceId, WorkSpaceToken}
import play.api.mvc.{BaseController, BodyParser}
import io.circe.generic.auto._
import cats.implicits._
import domains.bot.Bot.BotId

import scala.concurrent.ExecutionContext

final case class UninstallBotBody(team_id: String, api_app_id: String)

final case class UninstallBotCommand(workSpaceId: WorkSpaceId, botId: BotId)

trait UninstallBotBodyMapper extends JsonRequestMapper {
  this: BaseController =>
  def mapToUninstallBotCommand(implicit
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, UninstallBotCommand]] =
    mapToValueObject[UninstallBotBody, UninstallBotCommand] { body =>
      (
        WorkSpaceId.create(body.team_id).toValidatedNec,
        BotId.create(body.api_app_id).toValidatedNec
      ).mapN(UninstallBotCommand.apply)
        .toEither
        .leftMap(errors =>
          BadRequestError(
            errors.foldLeft("")((acc, cur) => acc + cur.errorMessage)
          )
        )

    }
}
