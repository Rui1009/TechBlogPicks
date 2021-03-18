package query.bots

trait BotsQueryProcessor {
  def findAll: Seq[BotsView]
}
