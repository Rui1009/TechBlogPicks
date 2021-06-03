package adapters.controllers.post

import adapters.AdapterError
import adapters.controllers.helpers.JsonHelper
import adapters.controllers.syntax.FutureSyntax
import com.google.inject.Inject
import infra.dao.slack.ChatDao
import infra.dao.slack.ChatDaoImpl._
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import query.posts.PostsQueryProcessor
import query.publishposts.{Post, PublishPostsQueryProcessor, PublishPostsView}
import usecases.{DeletePostsUseCase, RegisterPostUseCase}
import usecases.RegisterPostUseCase.Params
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class PostController @Inject() (
  val controllerComponents: ControllerComponents,
  registerPostUseCase: RegisterPostUseCase,
  publishPostsQueryProcessor: PublishPostsQueryProcessor,
  deleteUseCase: DeletePostsUseCase,
  chatDao: ChatDao,
  postsQueryProcessor: PostsQueryProcessor
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
      publishPost: PublishPostsView <- publishPosts
      channel                       <- publishPost.channels
//      text                           = publishPost.posts.foldLeft("今日のWinkieおすすめの記事はこちら！")((acc, curr) =>
//                                         acc + "\n" + curr.url
//                                       )
      post                          <- publishPost.posts
    } yield for {
      _ <-
        if (
          publishPost.posts.isEmpty || publishPost.token != "xoxb-2071830228691-2084235938177-G5V9UC0zfpHSbYkXIkYXSF3t"
        ) Future.unit
        else chatDao.postMessage(publishPost.token, channel, post.url)
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
            .toSuccessGetResponse
            .recoverError
      )
    }

  def index: Action[AnyContent] = Action.async {
    postsQueryProcessor.findAll
      .ifFailedThenToAdapterError("error in PostController.index")
      .toSuccessGetResponse
      .recoverError
  }
}
