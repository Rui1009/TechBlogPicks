package usecases

import com.google.inject.Inject
import domains.application.Application.ApplicationId
import domains.application.{ApplicationRepository, Post}
import domains.application.Post.{PostAuthor, PostPostedAt, PostTitle, PostUrl}
import domains.bot.Bot.BotId
import domains.post.PostRepository
import usecases.RegisterPostUseCase.Params

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

trait RegisterPostUseCase {
  def exec(params: Params): Future[Unit]
}

object RegisterPostUseCase {
  final case class Params(post: Post, applicationIds: Seq[ApplicationId])
}

final class RegisterPostUseCaseImpl @Inject() (
  applicationRepository: ApplicationRepository
)(implicit val ec: ExecutionContext)
    extends RegisterPostUseCase {
  override def exec(params: Params): Future[Unit] = postRepository
    .add(post, params.botIds)
    .ifFailThenToUseCaseError(
      "error while postRepository.add in register post use case"
    )
}
