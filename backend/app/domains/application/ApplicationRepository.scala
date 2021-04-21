package domains.application

import domains.application.Application.ApplicationId

import scala.concurrent.Future

trait ApplicationRepository {
  def find(applicationId: ApplicationId): Future[Option[Application]]
}
