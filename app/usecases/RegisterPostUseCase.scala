package usecases

import com.google.inject.Inject
import domains.bot.Bot.BotId
import domains.post.PostRepository
import domains.post.Post
import domains.post.Post._
import usecases.RegisterPostUseCase.Params

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait RegisterPostUseCase {
  def exec(params: Params): Future[Unit]
}

object RegisterPostUseCase {
  final case class Params(
      url: Option[PostUrl],
      title: PostTitle,
      postedAt: PostPostedAt,
      botIds: Seq[BotId]
  )
}

final class RegisterPostUseCaseImpl @Inject()(
    postRepository: PostRepository
)(implicit val ec: ExecutionContext)
    extends RegisterPostUseCase {
  override def exec(params: Params): Future[Unit] = {
    val post = Post(None, params.url, params.title, params.postedAt)
    postRepository
      .add(post, params.botIds)
      .ifFailThenToUseCaseError(
        "error while postRepository.add in register post use case"
      )
  }
}
