package domains.accesstokenpublisher

import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherTemporaryOauthCode

import scala.concurrent.Future

trait AccessTokenPublisherRepository {
  def find(
    code: AccessTokenPublisherTemporaryOauthCode
  ): Future[Option[AccessTokenPublisher]]
}
