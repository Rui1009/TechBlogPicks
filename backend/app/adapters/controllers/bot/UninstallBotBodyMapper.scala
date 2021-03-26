package adapters.controllers.bot

import adapters.{AdapterError, BadRequestError}
import adapters.controllers.helpers.JsonRequestMapper
import domains.workspace.WorkSpace.WorkSpaceToken
import play.api.mvc.{BaseController, BodyParser}
import io.circe.generic.auto._
import cats.implicits._

import scala.concurrent.ExecutionContext

final case class UninstallBotBody(
  token: String,
  challenge: String,
  `type`: String
)

final case class UninstallBotCommand(token: WorkSpaceToken, challenge: String)

trait UninstallBotBodyMapper extends JsonRequestMapper {
  this: BaseController =>
  def mapToUninstallBotCommand(implicit
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, UninstallBotCommand]] =
    mapToValueObject[UninstallBotBody, UninstallBotCommand] { body =>
      WorkSpaceToken
        .create(body.token)
        .map(t => UninstallBotCommand(t, body.challenge))
        .leftMap(error => BadRequestError(error.errorMessage))
    }
}
