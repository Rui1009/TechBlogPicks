package usecases

import com.google.inject.Inject
import domains.application.Application.ApplicationId
import domains.application.ApplicationRepository
import domains.post.Post.{PostAuthor, PostPostedAt, PostTitle, PostUrl}
import domains.post.{PostRepository, UnsavedPost}
import usecases.RegisterPostUseCase.Params

import scala.concurrent.{ExecutionContext, Future}

trait RegisterPostUseCase {
  def exec(params: Params): Future[Unit]
}

object RegisterPostUseCase {
  final case class Params(
    url: PostUrl,
    title: PostTitle,
    author: PostAuthor,
    postedAt: PostPostedAt,
    applicationIds: Seq[ApplicationId]
  )
}

final class RegisterPostUseCaseImpl @Inject() (
  postRepository: PostRepository,
  applicationRepository: ApplicationRepository
)(implicit val ec: ExecutionContext)
    extends RegisterPostUseCase {
  override def exec(params: Params): Future[Unit] = {
    val post =
      UnsavedPost(params.url, params.title, params.author, params.postedAt)

    for {
      savedPost           <-
        postRepository
          .save(post)
          .ifFailThenToUseCaseError(
            "error while postRepository.save in register post use case"
          )
      targetApplications  <-
        applicationRepository
          .filter(params.applicationIds)
          .ifNotExistsToUseCaseError(
            "error while get applications in register post use case"
          )
      assignedApplications = savedPost.assign(targetApplications)
      _                   <- applicationRepository
                               .save(assignedApplications, savedPost.id)
                               .ifFailThenToUseCaseError(
                                 "error while applicationRepository.add in register post use case"
                               )
    } yield ()
  }
}
