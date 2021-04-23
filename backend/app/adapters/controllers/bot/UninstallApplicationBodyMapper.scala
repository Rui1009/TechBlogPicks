package adapters.controllers.bot

import adapters.{AdapterError, BadRequestError}
import adapters.controllers.helpers.JsonRequestMapper
import domains.workspace.WorkSpace.WorkSpaceId
import play.api.mvc.{BaseController, BodyParser}
import io.circe.generic.auto._
import cats.implicits._
import domains.application.Application.ApplicationId

import scala.concurrent.ExecutionContext

final case class UninstallApplicationBody(team_id: String, api_app_id: String)

final case class UninstallApplicationCommand(
  workSpaceId: WorkSpaceId,
  applicationId: ApplicationId
)

trait UninstallApplicationBodyMapper extends JsonRequestMapper {
  this: BaseController =>
  def mapToUninstallApplicationCommand(implicit
    ec: ExecutionContext
  ): BodyParser[Either[AdapterError, UninstallApplicationCommand]] =
    mapToValueObject[UninstallApplicationBody, UninstallApplicationCommand] {
      body =>
        (
          WorkSpaceId.create(body.team_id).toValidatedNec,
          ApplicationId.create(body.api_app_id).toValidatedNec
        ).mapN(UninstallApplicationCommand.apply)
          .toEither
          .leftMap(errors =>
            BadRequestError(
              errors.foldLeft("")((acc, cur) => acc + cur.errorMessage)
            )
          )

    }
}
