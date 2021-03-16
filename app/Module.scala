import com.google.inject.AbstractModule
import domains.accesstokenpublisher.AccessTokenPublisherRepository
import domains.post.PostRepository
import infra.repositoryimpl.{
  AccessTokenPublisherRepositoryImpl,
  PostRepositoryImpl
}

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[PostRepository]).to(classOf[PostRepositoryImpl])
    bind(classOf[AccessTokenPublisherRepository])
      .to(classOf[AccessTokenPublisherRepositoryImpl])
  }
}
