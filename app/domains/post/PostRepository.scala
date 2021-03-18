package domains.post

import domains.bot.Bot.BotId
import domains.post.Post.PostId

import scala.concurrent.Future

trait PostRepository {
  def add(model: Post, botIds: Seq[BotId]): Future[Unit]

  def delete(ids: Seq[PostId]): Future[Unit]
}
