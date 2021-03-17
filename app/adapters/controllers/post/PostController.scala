package adapters.controllers.post

import adapters.AdapterError
import adapters.controllers.helpers.JsonHelper
import adapters.controllers.syntax.FutureSyntax
import com.google.inject.Inject
import io.circe.generic.auto._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import query.publishposts.PublishPostsQueryProcessor
import usecases.RegisterPostUseCase
import usecases.RegisterPostUseCase.Params
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}

class PostController @Inject() (
  val controllerComponents: ControllerComponents,
  registerPostUseCase: RegisterPostUseCase,
  publishPostsQueryProcessor: PublishPostsQueryProcessor,
  ws: WSClient
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

  def publish: Action[AnyContent] = Action.async {
    (for {
      publishPosts <- publishPostsQueryProcessor.findAll()
    } yield for {
      body <- PublishPostBody.fromViewModels(publishPosts)
    } yield for {
      _ <- ws.url("https://slack.com/api/chat.postMessage")
             .post(body.asJson.noSpaces)
    } yield ())
      .map(Future.sequence(_))
      .flatten
      .ifFailedThenToAdapterError("error in PostController.publish")
      .toSuccessGetResponse
      .recoverError
  }
}
