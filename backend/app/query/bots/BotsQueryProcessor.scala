package query.bots

import scala.concurrent.Future

trait BotsQueryProcessor {
  def findAll: Future[Seq[BotsView]]
}
