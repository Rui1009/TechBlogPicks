import com.google.inject.AbstractModule
import domains.accesstokenpublisher.AccessTokenPublisherRepository
import domains.bot.BotRepository
import domains.post.PostRepository
import infra.queryprocessorimpl.PublishPostsQueryProcessorImpl
import infra.repositoryimpl._
import query.publishposts.PublishPostsQueryProcessor
import usecases.{InstallBotUseCase, InstallBotUseCaseImpl, RegisterPostUseCase, RegisterPostUseCaseImpl}

class Module extends AbstractModule {
  override def configure(): Unit = {
    // repo impl
    bind(classOf[PostRepository]).to(classOf[PostRepositoryImpl])
    bind(classOf[AccessTokenPublisherRepository])
      .to(classOf[AccessTokenPublisherRepositoryImpl])
    bind(classOf[BotRepository]).to(classOf[BotRepositoryImpl])

    // use case impl
    bind(classOf[RegisterPostUseCase]).to(classOf[RegisterPostUseCaseImpl])
    bind(classOf[InstallBotUseCase]).to(classOf[InstallBotUseCaseImpl])

    // query processor impl
    bind(classOf[PublishPostsQueryProcessor])
      .to(classOf[PublishPostsQueryProcessorImpl])
  }
}
