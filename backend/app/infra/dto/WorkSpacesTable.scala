package infra.dto
// AUTO-GENERATED Slick data model for table WorkSpaces
trait WorkSpacesTable {

  self: Tables =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** Entity class storing rows of table WorkSpaces
    *  @param token Database column token SqlType(text), PrimaryKey
    *  @param botId Database column bot_id SqlType(text)
    *  @param teamId Database column team_id SqlType(text), Default(hoge)
    */
  case class WorkSpacesRow(
    token: String,
    botId: String,
    teamId: String = "hoge"
  )

  /** GetResult implicit for fetching WorkSpacesRow objects using plain SQL queries */
  implicit def GetResultWorkSpacesRow(implicit
    e0: GR[String]
  ): GR[WorkSpacesRow] = GR { prs =>
    import prs._
    WorkSpacesRow.tupled((<<[String], <<[String], <<[String]))
  }

  /** Table description of table work_spaces. Objects of this class serve as prototypes for rows in queries. */
  class WorkSpaces(_tableTag: Tag)
      extends profile.api.Table[WorkSpacesRow](_tableTag, "work_spaces") {
    def * =
      (token, botId, teamId) <> (WorkSpacesRow.tupled, WorkSpacesRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(token), Rep.Some(botId), Rep.Some(teamId))).shaped.<>(
      { r =>
        import r._; _1.map(_ => WorkSpacesRow.tupled((_1.get, _2.get, _3.get)))
      },
      (_: Any) =>
        throw new Exception("Inserting into ? projection not supported.")
    )

    /** Database column token SqlType(text), PrimaryKey */
    val token: Rep[String] = column[String]("token", O.PrimaryKey)

    /** Database column bot_id SqlType(text) */
    val botId: Rep[String] = column[String]("bot_id")

    /** Database column team_id SqlType(text), Default(hoge) */
    val teamId: Rep[String] = column[String]("team_id", O.Default("hoge"))
  }

  /** Collection-like TableQuery object for table WorkSpaces */
  lazy val WorkSpaces = new TableQuery(tag => new WorkSpaces(tag))
}
