package domains.application

import domains.application.Application.ApplicationId
import domains.bot.Bot.BotId
import domains.post.Post.PostId

import scala.concurrent.Future

trait ApplicationRepository {
  def find(applicationId: ApplicationId): Future[Option[Application]]
  def filter(applicationIds: Seq[ApplicationId]): Future[Seq[Application]]
  def update(application: Application): Future[Unit]
  def save(applications: Seq[Application], postId: PostId): Future[Unit]
}
