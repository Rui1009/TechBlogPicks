package domains.post

import domains.post.Post.PostId

import scala.concurrent.Future

trait PostRepository {
  def save(model: Post): Future[Post]

  def delete(ids: Seq[PostId]): Future[Unit]
}
