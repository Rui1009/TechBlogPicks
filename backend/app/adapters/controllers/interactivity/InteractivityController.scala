package adapters.controllers.interactivity

import adapters.AdapterError
import adapters.controllers.helpers.JsonHelper
import com.google.inject.Inject
import adapters.controllers.syntax.AllSyntax
import play.api.mvc.{Action, BaseController, ControllerComponents}
import usecases.JoinChannelUseCase

import scala.concurrent.{ExecutionContext, Future}

class InteractivityController @Inject() (
  val controllerComponents: ControllerComponents,
  joinChannelUseCase: JoinChannelUseCase
)(implicit val ec: ExecutionContext)
    extends BaseController with JsonHelper with AllSyntax
    with InteractivityBodyMapper {
  def handleInteractivity: Action[Either[AdapterError, InteractivityCommand]] =
    Action.async(mapToInteractivityCommand) { implicit request =>
      request.body.fold(
        e => Future.successful(responseError(e)),
        { case command: ChannelSelectActionInteractivityCommand =>
          println(command)
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
