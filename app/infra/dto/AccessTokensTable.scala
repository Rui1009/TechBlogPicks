package infra.dto
// AUTO-GENERATED Slick data model for table AccessTokens
trait AccessTokensTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table AccessTokens
   *  @param token Database column token SqlType(text), PrimaryKey
   *  @param botId Database column bot_id SqlType(text) */
  case class AccessTokensRow(token: String, botId: String)
  /** GetResult implicit for fetching AccessTokensRow objects using plain SQL queries */
  implicit def GetResultAccessTokensRow(implicit e0: GR[String]): GR[AccessTokensRow] = GR{
    prs => import prs._
    AccessTokensRow.tupled((<<[String], <<[String]))
  }
  /** Table description of table access_tokens. Objects of this class serve as prototypes for rows in queries. */
  class AccessTokens(_tableTag: Tag) extends profile.api.Table[AccessTokensRow](_tableTag, "access_tokens") {
    def * = (token, botId) <> (AccessTokensRow.tupled, AccessTokensRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(token), Rep.Some(botId))).shaped.<>({r=>import r._; _1.map(_=> AccessTokensRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column token SqlType(text), PrimaryKey */
    val token: Rep[String] = column[String]("token", O.PrimaryKey)
    /** Database column bot_id SqlType(text) */
    val botId: Rep[String] = column[String]("bot_id")
  }
  /** Collection-like TableQuery object for table AccessTokens */
  lazy val AccessTokens = new TableQuery(tag => new AccessTokens(tag))
}
