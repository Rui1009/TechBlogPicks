package domains.accesstokenpublisher

import scala.concurrent.Future

trait AccessTokenPublisherRepository {
  def publish(code: String): Future[AccessTokenPublisher]
}
