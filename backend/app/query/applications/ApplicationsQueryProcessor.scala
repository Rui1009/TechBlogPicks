package query.applications

import scala.concurrent.Future

trait ApplicationsQueryProcessor {
  def findAll: Future[Seq[ApplicationsView]]
}
