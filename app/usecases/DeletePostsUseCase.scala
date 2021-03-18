package usecases

import com.google.inject.Inject
import domains.post.Post.PostId
import domains.post.PostRepository
import usecases.DeletePostsUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait DeletePostsUseCase {
  def exec(params: Params): Future[Unit]
}

object DeletePostsUseCase {
  final case class Params(ids: Seq[PostId])
}

final class DeletePostsUseCaseImpl @Inject() (postRepository: PostRepository)(
  implicit val ec: ExecutionContext
) extends DeletePostsUseCase {
  override def exec(params: Params): Future[Unit] = postRepository
    .delete(params.ids)
    .ifFailThenToUseCaseError(
      "error while postRepository.delete in delete posts use case"
    )
}
