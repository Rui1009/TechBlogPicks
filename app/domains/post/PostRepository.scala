package domains.post

import domains.bot.Bot.BotId
import infra.InfraError

import scala.concurrent.Future

trait PostRepository {
  def add(model: Post, botIds: Seq[BotId]): Future[Unit]

  def delete(ids: Seq[BotId]): Future[Unit]
}
