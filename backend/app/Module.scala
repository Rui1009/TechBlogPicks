import com.google.inject.AbstractModule
import domains.application.ApplicationRepository
import domains.workspace.WorkSpaceRepository
import domains.post.PostRepository
import infra.dao.slack._
import infra.queryprocessorimpl._
import infra.repositoryimpl._
import query.bots.BotsQueryProcessor
import query.posts.PostsQueryProcessor
import query.publishposts.PublishPostsQueryProcessor
import usecases._

class Module extends AbstractModule {
  override def configure(): Unit = {
    // repo impl
    bind(classOf[PostRepository]).to(classOf[PostRepositoryImpl])
    bind(classOf[WorkSpaceRepository]).to(classOf[WorkSpaceRepositoryImpl])
    bind(classOf[ApplicationRepository]).to(classOf[ApplicationRepositoryImpl])

    // use case impl
    bind(classOf[RegisterPostUseCase]).to(classOf[RegisterPostUseCaseImpl])
    bind(classOf[InstallApplicationUseCase])
      .to(classOf[InstallApplicationUseCaseImpl])
    bind(classOf[DeletePostsUseCase]).to(classOf[DeletePostsUseCaseImpl])
    bind(classOf[UpdateApplicationClientInfoUseCase])
      .to(classOf[UpdateApplicationClientInfoUseCaseImpl])
    bind(classOf[UninstallApplicationUseCase])
      .to(classOf[UninstallApplicationUseCaseImpl])
    bind(classOf[PostOnboardingMessageUseCase])
      .to(classOf[PostOnboardingMessageUseCaseImpl])
    bind(classOf[JoinChannelUseCase]).to(classOf[JoinChannelUseCaseImpl])

    // query processor impl
    bind(classOf[PublishPostsQueryProcessor])
      .to(classOf[PublishPostsQueryProcessorImpl])
    bind(classOf[BotsQueryProcessor]).to(classOf[BotsQueryProcessorImpl])
    bind(classOf[PostsQueryProcessor]).to(classOf[PostsQueryProcessorImpl])

    // dao
    bind(classOf[ChatDao]).to(classOf[ChatDaoImpl])
    bind(classOf[UsersDao]).to(classOf[UsersDaoImpl])
    bind(classOf[TeamDao]).to(classOf[TeamDaoImpl])
    bind(classOf[ConversationDao]).to(classOf[ConversationDaoImpl])
  }
}
