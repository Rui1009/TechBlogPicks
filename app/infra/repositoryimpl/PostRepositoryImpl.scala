package infra.repositoryimpl

import com.google.inject.Inject
import domains.bot.Bot.BotId
import domains.post.{Post, PostRepository}
import infra.dto.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import infra.syntax.all._

import scala.concurrent.{ExecutionContext, Future}

class PostRepositoryImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile] with PostRepository
    with API {
  override def add(model: Post, botIds: Seq[BotId]): Future[Unit] = {
    val nowUnix      = System.currentTimeMillis / 1000
    val newPost      = model.toRow(nowUnix)
    val postsInsertQ =
      Posts.returning(Posts.map(_.id)).into((_, id) => id) += newPost
    val query        = for {
      postId <- postsInsertQ
      news    = botIds.map(id => BotsPostsRow(0, id.value.value, postId))
      _      <- BotsPosts ++= news
    } yield ()

    db.run(query.transactionally)
  }.ifFailedThenToInfraError("error while PostRepository.add")

  override def delete(ids: Seq[BotId]): Future[Unit] = ???
}
