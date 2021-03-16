import com.google.inject.AbstractModule
import domains.accesstokenpublisher.AccessTokenPublisherRepository
import domains.post.PostRepository
import infra.queryprocessorimpl.PublishPostsQueryProcessorImpl
import infra.repositoryimpl._
import query.publishposts.PublishPostsQueryProcessor
import usecases.{RegisterPostUseCase, RegisterPostUseCaseImpl}

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[PostRepository]).to(classOf[PostRepositoryImpl])
    bind(classOf[AccessTokenPublisherRepository])
      .to(classOf[AccessTokenPublisherRepositoryImpl])
    bind(classOf[RegisterPostUseCase]).to(classOf[RegisterPostUseCaseImpl])

    bind(classOf[PublishPostsQueryProcessor])
      .to(classOf[PublishPostsQueryProcessorImpl])
  }
}
