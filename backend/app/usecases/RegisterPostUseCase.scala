package usecases

import com.google.inject.Inject
import domains.application.Application.ApplicationId
import domains.application.ApplicationRepository
import domains.post.{Post, PostRepository}
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
  postRepository: PostRepository,
  applicationRepository: ApplicationRepository
)(implicit val ec: ExecutionContext)
    extends RegisterPostUseCase {
  override def exec(params: Params): Future[Unit] =
    //Todo: postはadapterではなくてusecase層で作る
    for {
      savedPost           <-
        postRepository
          .save(params.post)
          .ifFailThenToUseCaseError(
            "error while postRepository.save in register post use case"
          )
      targetApplications  <-
        applicationRepository.filter(params.applicationIds) // エラーハンドリング
      assignedApplications = savedPost.assign(targetApplications)
      _                   <- applicationRepository
                               .add(assignedApplications)
                               .ifFailThenToUseCaseError(
                                 "error while applicationRepository.add in register post use case"
                               )
    } yield ()
}
