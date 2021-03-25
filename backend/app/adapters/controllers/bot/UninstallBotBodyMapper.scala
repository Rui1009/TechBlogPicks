package adapters.controllers.bot

import adapters.{AdapterError, BadRequestError}
import adapters.controllers.helpers.JsonRequestMapper
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import play.api.mvc.{BaseController, BodyParser}
import cats.implicits._

import scala.concurrent.ExecutionContext

final case class UninstallBotBody(
  token: String,
  challenge: String,
  `type`: String
)

final case class UninstallBotCommand(token: AccessTokenPublisherToken)

trait UninstallBotBodyMapper extends JsonRequestMapper {
  this: BaseController =>
  def mapToUninstallBotCommand(implicit
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, UninstallBotCommand]] =
    mapToValueObject[UninstallBotBody, UninstallBotCommand] { body =>
      AccessTokenPublisherToken
        .create(body.token)
        .map(UninstallBotCommand)
        .leftMap(error => BadRequestError(error.errorMessage))
    }
}
