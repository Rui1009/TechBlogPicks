package domains.post

import infra.InfraError

import scala.concurrent.Future

trait PostRepository {
  def add(model: Post): Future[Unit]
}
