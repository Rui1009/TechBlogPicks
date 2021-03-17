package adapters.controllers.bot

import adapters.{AdapterError, BadRequestError}
import adapters.controllers.helpers.JsonRequestMapper
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherTemporaryOauthCode
import domains.bot.Bot.BotId
import play.api.mvc.BaseController
import cats.implicits._
import domains.DomainError

final case  class InstallBotBody(
  code: String,
  botId: String
)

final case class InstallBotCommand(
  code: AccessTokenPublisherTemporaryOauthCode,
  botId: BotId
)

trait BotInstallBodyMapper extends JsonRequestMapper[InstallBotBody, InstallBotCommand] {
  this: BaseController =>
  override def mapToValueObject(body: InstallBotBody): Either[AdapterError, InstallBotCommand] = (
    AccessTokenPublisherTemporaryOauthCode.create(body.code).toValidatedNec,
    BotId.create(body.botId).toValidatedNec
  ).mapN(InstallBotCommand.apply)
    .toEither
    .leftMap(errors => BadRequestError(
      errors.foldLeft("")((acc, cur: DomainError) => acc + cur.errorMessage)
    ))
}
