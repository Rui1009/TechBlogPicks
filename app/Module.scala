import com.google.inject.AbstractModule
import domains.post.PostRepository
import infra.repositoryimpl.PostRepositoryImpl

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[PostRepository]).to(classOf[PostRepositoryImpl])
  }
}
