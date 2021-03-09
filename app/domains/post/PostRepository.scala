package domains.post

import infra.InfraError

import scala.concurrent.Future

trait PostRepository {
  def Add(model: Post): Future[Either[InfraError, Unit]]
}
