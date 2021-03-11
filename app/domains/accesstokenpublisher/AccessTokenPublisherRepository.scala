package domains.accesstokenpublisher

import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherTemporaryOauthCode
import infra.InfraError

import scala.concurrent.Future

trait AccessTokenPublisherRepository {
  def publish(
    code: AccessTokenPublisherTemporaryOauthCode
  ): Future[Either[InfraError, AccessTokenPublisher]]
}
