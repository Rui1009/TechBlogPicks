package query.applications

case class ApplicationsView(
  id: String,
  name: String,
  clientId: Option[String],
  clientSecret: Option[String]
)
