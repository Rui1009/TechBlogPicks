package infra.queryprocessorimpl

import com.google.inject.Inject
import infra.dto.Tables._
import infra.syntax.all._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import query.posts.{PostsQueryProcessor, PostsView}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API

import scala.concurrent.{ExecutionContext, Future}

class PostsQueryProcessorImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile] with API
    with PostsQueryProcessor {
  override def findAll: Future[Seq[PostsView]] = for {
    queryResult <- db.run {
                     Posts.sortBy(_.createdAt.desc).result
                   }.ifFailedThenToInfraError(
                     "error while PostsQueryProcessor.findAll"
                   )
  } yield queryResult.map(r => PostsView(r.id, r.url, r.title, r.author, r.postedAt, r.createdAt))
}
