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
  object EventType {
    val UrlVerification = "url_verification"
    val AppUninstalled  = "app_uninstalled"
  }

  def handleEvent: Action[Either[AdapterError, EventRootCommand]] =
    Action.async(mapToEventCommand) { implicit request =>
      request.body.fold(
        e => Future.successful(responseError(e)),
        body =>
          (body.eventType match {
            case EventType.UrlVerification => urlVerification(body)
            case _                         => body.event.flatMap { e =>
                e.eventType match {
                  case EventType.AppUninstalled => appUninstalled(body)
                }
              }
          }).toSuccessResponseForEvent.recoverError
      )
    }

  private def urlVerification(body: EventRootCommand) =
    body.challenge.map { c =>
      Future.successful(
        Ok(Json.obj("challenge" -> Json.fromString(c)).noSpaces)
          .as(JSON)
          .withHeaders("Access-Control-Allow-Origin" -> "*")
      )
    }

  private def appUninstalled(body: EventRootCommand) = for {
    botId       <- body.apiAppId
    workSpaceId <- body.teamId
  } yield uninstallBotUseCase.exec(UninstallBotUseCase.Params(botId, workSpaceId)).ifFailedThenToAdapterError("error in EventController.appUninstalled").toSuccessPostResponse
}
