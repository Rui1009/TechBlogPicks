package query.publishposts

import scala.concurrent.Future

trait PublishPostsQueryProcessor {
  def findAll(): Future[Seq[PublishPostsView]]
}
