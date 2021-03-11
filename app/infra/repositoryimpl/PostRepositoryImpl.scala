package infra.repositoryimpl

import com.google.inject.Inject
import domains.bot.Bot
import domains.bot.Bot.BotId
import domains.post.{Post, PostRepository}
import infra.dto.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile
import infra.syntax.domain._
import slick.jdbc.PostgresProfile.API

import scala.concurrent.{ExecutionContext, Future}

class PostRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile] with PostRepository
    with API {
  override def add(model: Post, botIds: Seq[BotId]): Future[Unit] = {
    val postsInsertAction     = Posts.returning(Posts.map(_.id)) += model.toRow(1)
    val botsPostsInsertAction =
      for {
        postId <- postsInsertAction
      } yield for {
        botId <- botIds
      } yield BotsPosts += BotsPostsRow(0, botId.value.value, postId)
    db.run(DBIO.seq(postsInsertAction, botsPostsInsertAction).transactionally)
  }
}
