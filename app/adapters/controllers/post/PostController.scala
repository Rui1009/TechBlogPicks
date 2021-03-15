package adapters.controllers.post

import adapters.AdapterError
import adapters.controllers.helpers.JsonHelper
import adapters.controllers.syntax.FutureSyntax
import com.google.inject.Inject
import io.circe.generic.auto._
import play.api.mvc.{Action, BaseController, ControllerComponents}
import usecases.RegisterPostUseCase
import usecases.RegisterPostUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

class PostController @Inject() (
  val controllerComponents: ControllerComponents,
  registerPostUseCase: RegisterPostUseCase
)(implicit val ec: ExecutionContext)
    extends BaseController with PostCreateBodyMapper with FutureSyntax
    with JsonHelper {
  def create: Action[Either[AdapterError, CreatePostCommand]] =
    Action.async(mapToCommand) { implicit request =>
      request.body.fold(
        e => Future.successful(responseError(e)),
        body =>
          registerPostUseCase
            .exec(
              Params(
                body.url,
                body.title,
                body.author,
                body.postedAt,
                body.botIds
              )
            )
            .ifFailedThenToAdapterError("error in PostController.create")
            .toSuccessPostResponse
            .recoverError
      )
    }
}
