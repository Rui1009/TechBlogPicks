package adapters.controllers.interactivity

import adapters.AdapterError
import adapters.controllers.helpers.JsonHelper
import com.google.inject.Inject
import adapters.controllers.syntax.AllSyntax
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, BaseController, ControllerComponents}
import usecases.JoinChannelUseCase

import scala.concurrent.{ExecutionContext, Future}

class InteractivityController @Inject() (
  val controllerComponents: ControllerComponents,
  joinChannelUseCase: JoinChannelUseCase,
  ws: WSClient
)(implicit val ec: ExecutionContext)
    extends BaseController with JsonHelper with AllSyntax
    with InteractivityBodyMapper {

  def convertRequest = Action { implicit request =>
    val converted = request.body.toString
      .replace("AnyContentAsFormUrlEncoded(ListMap(", "[")
      .replace("-> List(", ":[")
      .replace("[payload", "[{\"payload\"")
      .dropRight(3) + "]}]"

    ws.url("https://winkie.herokuapp.com/events")
      .withBody(Json.obj("body" -> Json.parse(converted)))
      .execute("POST")
    Ok("ol")
  }

  def handleInteractivity = Action { implicit request =>
    println("debug")
    println(request)
    println(request.body.toString)
    Ok("ok")
  }

//    Action.async(mapToInteractivityCommand) { implicit request =>
//      request.body.fold(
//        e => Future.successful(responseError(e)),
//        { case command: ChannelSelectActionInteractivityCommand =>
//          println("command exec")
//          channelSelect(command)
//        }
//      )
//    }

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
