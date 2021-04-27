package infra.repositoryimpl

import com.google.inject.Inject
import domains.post.Post.{PostAuthor, PostId, PostPostedAt, PostTitle, PostUrl}
import domains.post.{Post, PostRepository, UnsavedPost}
import eu.timepit.refined.api.Refined
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
  override def save(model: UnsavedPost): Future[Post] = {
    val nowUnix = System.currentTimeMillis / 1000
    val newPost = model.toRow(nowUnix)
    db.run {
      Posts
        .returning(
          Posts.map(post => (post.id, post.url, post.title, post.author, post))
        )
        .into((_, id) => id) += newPost
    }.map(post =>
      Post(
        PostId(Refined.unsafeApply(post._1)),
        PostUrl(Refined.unsafeApply(post._2)),
        PostTitle(Refined.unsafeApply(post._3)),
        PostAuthor(Refined.unsafeApply(post._4)),
        PostPostedAt(Refined.unsafeApply(post._5))
      )
    ).ifFailedThenToInfraError("error while PostRepository.save")
  }

  override def delete(ids: Seq[PostId]): Future[Unit] = db.run {
    DBIO
      .seq(
        BotsPosts.filter(_.postId.inSet(ids.map(_.value.value))).delete,
        Posts.filter(_.id.inSet(ids.map(_.value.value))).delete
      )
      .transactionally
  }.ifFailedThenToInfraError("error while PostRepository.delete")
}
