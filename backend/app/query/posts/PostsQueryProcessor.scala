package query.posts

import scala.concurrent.Future

trait PostsQueryProcessor {
  def findAll: Future[Seq[PostsView]]
}
