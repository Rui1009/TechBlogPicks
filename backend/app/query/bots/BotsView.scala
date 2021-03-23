package query.bots

case class BotsView(
  id: String,
  name: String,
  clientId: Option[String],
  clientSecret: Option[String]
)
