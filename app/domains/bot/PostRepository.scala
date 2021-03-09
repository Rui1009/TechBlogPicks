package domains.bot

import domains.post.Post

import scala.concurrent.Future

trait PostRepository {
  def Add(model: Post): Future[Unit]
}
