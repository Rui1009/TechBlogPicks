package infra.queryprocessorimpl

import com.google.inject.Inject
import infra.dto.Tables._
import infra.lib.HasDB
import infra.syntax.all._
import play.api.db.slick.DatabaseConfigProvider
import query.posts.{PostsQueryProcessor, PostsView}

import scala.concurrent.{ExecutionContext, Future}

class PostsQueryProcessorImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit val ec: ExecutionContext)
    extends HasDB with PostsQueryProcessor {
  override def findAll: Future[Seq[PostsView]] = for {
    queryResult <- db.run {
                     Posts.sortBy(_.createdAt.desc).result
                   }.ifFailedThenToInfraError(
                     "error while PostsQueryProcessor.findAll"
                   )
  } yield queryResult.map(r => PostsView(r.id, r.url, r.title, r.author, r.postedAt, r.createdAt))
}
