package adapters.controllers.interactivity

import adapters.AdapterError
import adapters.controllers.helpers.JsonHelper
import com.google.inject.Inject
import adapters.controllers.syntax.AllSyntax
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import usecases.JoinChannelUseCase

import scala.concurrent.{ExecutionContext, Future}

class InteractivityController @Inject() (
  val controllerComponents: ControllerComponents,
  joinChannelUseCase: JoinChannelUseCase,
  ws: WSClient
)(implicit val ec: ExecutionContext)
    extends BaseController with JsonHelper with AllSyntax
    with InteractivityBodyMapper {

  def convertRequest: Action[AnyContent] = Action.async { implicit request =>
    val converted = request.body.toString
      .replace("AnyContentAsFormUrlEncoded(ListMap(", "[")
      .replace("-> List(", ":[")
      .replace("[payload", "[{\"payload\"")
      .dropRight(3) + "]}]"

    ws.url("https://winkie.herokuapp.com/interactivity")
      .withBody(Json.parse(converted))
      .execute("POST")
      .map(_ => Ok)
  }

  def handleInteractivity: Action[Either[AdapterError, InteractivityCommand]] =
    Action.async(mapToInteractivityCommand) { implicit request =>
      request.body.fold(
        e => Future.successful(responseError(e)),
        { case command: ChannelSelectActionInteractivityCommand =>
          channelSelect(command)
        }
      )
    }

  private def channelSelect(command: ChannelSelectActionInteractivityCommand) =
    joinChannelUseCase
      .exec(
        JoinChannelUseCase
          .Params(command.channelId, command.applicationId, command.workSpaceId)
      )
      .ifFailedThenToAdapterError(
        "error in InteractivityController.chanelSelect"
      )
      .toSuccessPostResponse
      .recoverError
}
