package usecases

import com.google.inject.Inject
import domains.post.PostRepository
import domains.post.Post
import domains.post.Post._
import usecases.RegisterPostUseCase.Params
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait RegisterPostUseCase {
  def exec(params: Params): Future[Either[UseCaseError, Unit]]
}

object RegisterPostUseCase {
  final case class Params(
      url: Option[PostUrl],
      title: PostTitle,
      postedAt: PostPostedAt
  )
}

final class RegisterPostUseCaseImpl @Inject()(
    postRepository: PostRepository
)(implicit val ec: ExecutionContext)
    extends RegisterPostUseCase {
  override def exec(params: Params): Future[Either[SystemError, Unit]] = {
    val post = Post(None, params.url, params.title, params.postedAt)
    postRepository.Add(post).map {
      case Right(v) => Right(v)
      case Left(e) =>
        Left(
          SystemError("error while postRepository.add" + "\n" + e.errorMessage))
    }
  }
}
