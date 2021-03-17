package infra.repositoryimpl

import com.google.inject.Inject
import domains.bot.{Bot, BotRepository}

import scala.concurrent.Future

class BotRepositoryImpl @Inject() extends BotRepository {
  override def find(botId: Bot.BotId): Future[Bot] = ???

  override def update(bot: Bot): Future[Unit] = ???

}
