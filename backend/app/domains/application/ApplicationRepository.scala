package domains.application

import domains.application.Application.ApplicationId

import scala.concurrent.Future

trait ApplicationRepository {
  def find(applicationId: ApplicationId): Future[Option[Application]]
  def filter(applicationIds: Seq[ApplicationId]): Future[Seq[Application]]
  def update(application: Application): Future[Unit]
  def add(applications: Seq[Application]): Future[Unit]
//  def add(post: Post, applicationIds: Seq[ApplicationId]): Future[Unit] will delete
}
