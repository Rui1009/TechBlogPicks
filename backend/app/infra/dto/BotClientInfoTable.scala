package infra.dto
// AUTO-GENERATED Slick data model for table BotClientInfo
trait BotClientInfoTable {

  self: Tables =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** Entity class storing rows of table BotClientInfo
    *  @param botId Database column bot_id SqlType(text), PrimaryKey
    *  @param clientId Database column client_id SqlType(text), Default(None)
    *  @param clientSecret Database column client_secret SqlType(text), Default(None)
    */
  case class BotClientInfoRow(
    botId: String,
    clientId: Option[String] = None,
    clientSecret: Option[String] = None
  )

  /** GetResult implicit for fetching BotClientInfoRow objects using plain SQL queries */
  implicit def GetResultBotClientInfoRow(implicit
    e0: GR[String],
    e1: GR[Option[String]]
  ): GR[BotClientInfoRow] = GR { prs =>
    import prs._
    BotClientInfoRow.tupled((<<[String], <<?[String], <<?[String]))
  }

  /** Table description of table bot_client_info. Objects of this class serve as prototypes for rows in queries. */
  class BotClientInfo(_tableTag: Tag)
      extends profile.api.Table[BotClientInfoRow](
        _tableTag,
        "bot_client_info"
      ) {
    def * =
      (
        botId,
        clientId,
        clientSecret
      ) <> (BotClientInfoRow.tupled, BotClientInfoRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(botId), clientId, clientSecret)).shaped.<>(
      { r =>
        import r._; _1.map(_ => BotClientInfoRow.tupled((_1.get, _2, _3)))
      },
      (_: Any) =>
        throw new Exception("Inserting into ? projection not supported.")
    )

    /** Database column bot_id SqlType(text), PrimaryKey */
    val botId: Rep[String] = column[String]("bot_id", O.PrimaryKey)

    /** Database column client_id SqlType(text), Default(None) */
    val clientId: Rep[Option[String]] =
      column[Option[String]]("client_id", O.Default(None))

    /** Database column client_secret SqlType(text), Default(None) */
    val clientSecret: Rep[Option[String]] =
      column[Option[String]]("client_secret", O.Default(None))

    /** Uniqueness Index over (clientId) (database name bot_client_info_client_id_key) */
    val index1 = index("bot_client_info_client_id_key", clientId, unique = true)

    /** Uniqueness Index over (clientSecret) (database name bot_client_info_client_secret_key) */
    val index2 =
      index("bot_client_info_client_secret_key", clientSecret, unique = true)
  }

  /** Collection-like TableQuery object for table BotClientInfo */
  lazy val BotClientInfo = new TableQuery(tag => new BotClientInfo(tag))
}
