package adapters.controllers.event

import adapters.AdapterError
import adapters.controllers.helpers.JsonHelper
import adapters.controllers.syntax.AllSyntax
import com.google.inject.Inject
import io.circe.Json
import play.api.mvc._
import usecases.UninstallBotUseCase

import scala.concurrent.{ExecutionContext, Future}

class EventController @Inject() (
  val controllerComponents: ControllerComponents,
  uninstallBotUseCase: UninstallBotUseCase
)(implicit val ec: ExecutionContext)
    extends BaseController with JsonHelper with EventBodyMapper with AllSyntax {
  def handleEvent: Action[Either[AdapterError, EventCommand]] =
    Action.async(mapToEventCommand) { implicit request =>
      request.body.fold(
        e => Future.successful(responseError(e)),
        {
          case command: AppUninstalledEventCommand  => appUninstalled(command)
          case command: UrlVerificationEventCommand => urlVerification(command)
        }
      )
    }

  private def urlVerification(command: UrlVerificationEventCommand) = Future
    .successful(
      Ok(Json.obj("challenge" -> Json.fromString(command.challenge)).noSpaces)
        .as(JSON)
        .withHeaders("Access-Control-Allow-Origin" -> "*")
    )
    .recoverError

  private def appUninstalled(command: AppUninstalledEventCommand) =
    uninstallBotUseCase
      .exec(UninstallBotUseCase.Params(command.botId, command.workSpaceId))
      .ifFailedThenToAdapterError("error in EventController.appUninstalled")
      .toSuccessPostResponse
      .recoverError
}
