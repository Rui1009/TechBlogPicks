package usecases

import com.google.inject.Inject
import domains.bot.PostRepository
import domains.post.Post
import domains.post.Post._
import usecases.RegisterPostUseCase.Params
import scala.concurrent.Future

trait RegisterPostUseCase {
  def exec(params: Params): Future[Unit]
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
) extends RegisterPostUseCase {
  override def exec(params: Params): Future[Unit] = {
    val post = Post(None, params.url, params.title, params.postedAt)
    ???
  }
}
