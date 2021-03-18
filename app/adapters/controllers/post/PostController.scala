package adapters.controllers.post

import adapters.AdapterError
import adapters.controllers.helpers.JsonHelper
import adapters.controllers.syntax.FutureSyntax
import com.google.inject.Inject
import infra.dao.slack.ChatDao
import infra.dao.slack.ChatDaoImpl._
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import query.publishposts.PublishPostsQueryProcessor
import usecases.{DeletePostsUseCase, RegisterPostUseCase}
import usecases.RegisterPostUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

class PostController @Inject() (
  val controllerComponents: ControllerComponents,
  registerPostUseCase: RegisterPostUseCase,
  publishPostsQueryProcessor: PublishPostsQueryProcessor,
  deleteUseCase: DeletePostsUseCase,
  chatDao: ChatDao
)(implicit val ec: ExecutionContext)
    extends BaseController with PostCreateBodyMapper with DeletePostsBodyMapper
    with FutureSyntax with JsonHelper {
  def create: Action[Either[AdapterError, CreatePostCommand]] =
    Action.async(mapToCreateCommand) { implicit request =>
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
      body <- PostMessageBody.fromViewModels(publishPosts, "今日の記事")
    } yield for {
      _ <- chatDao.postMessage(body)
    } yield ())
      .map(Future.sequence(_))
      .flatMap(_.map(_ => ()))
      .ifFailedThenToAdapterError("error in PostController.publish")
      .toSuccessGetResponse
      .recoverError
  }

  def delete: Action[Either[AdapterError, DeletePostsCommand]] =
    Action.async(mapToDeleteCommand) { implicit request =>
      request.body.fold(
        e => Future.successful(responseError(e)),
        body =>
          deleteUseCase
            .exec(DeletePostsUseCase.Params(body.ids))
            .ifFailedThenToAdapterError("error in PostController.delete")
            .toSuccessPostResponse
            .recoverError
      )
    }
}
