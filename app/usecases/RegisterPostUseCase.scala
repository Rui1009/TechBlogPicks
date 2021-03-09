package usecases

import com.google.inject.Inject
import domains.bot.PostRepository
import domains.post.Post._

import scala.concurrent.Future

trait RegisterPostUseCase {
  def exec(): Future[Unit]
}

object RegisterPostUseCase {
  final case class Params(url: PostUrl, title: PostTitle, postedAt: PostedAt)
}

final class RegisterPostUseCaseImpl @Inject()(
    postRepository: PostRepository
) extends RegisterPostUseCase {
  override def exec(): Future[Unit] = ???
}
