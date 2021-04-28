package domains.post

import domains.post.Post.PostId

import scala.concurrent.Future

trait PostRepository {
  def save(model: UnsavedPost): Future[Post]

  def delete(ids: Seq[PostId]): Future[Unit]
}
