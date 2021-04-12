import com.google.inject.AbstractModule
import domains.workspace.WorkSpaceRepository
import domains.bot.BotRepository
import domains.message.MessageRepository
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
    bind(classOf[BotRepository]).to(classOf[BotRepositoryImpl])
    bind(classOf[MessageRepository]).to(classOf[MessageRepositoryImpl])

    // use case impl
    bind(classOf[RegisterPostUseCase]).to(classOf[RegisterPostUseCaseImpl])
    bind(classOf[InstallBotUseCase]).to(classOf[InstallBotUseCaseImpl])
    bind(classOf[DeletePostsUseCase]).to(classOf[DeletePostsUseCaseImpl])
    bind(classOf[UpdateBotClientInfoUseCase])
      .to(classOf[UpdateBotClientInfoUseCaseImpl])
    bind(classOf[UninstallBotUseCase]).to(classOf[UninstallBotUseCaseImpl])

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
