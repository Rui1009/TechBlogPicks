package infra.dto
// AUTO-GENERATED Slick data model for table BotsPosts
trait BotsPostsTable {

  self: Tables =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** Entity class storing rows of table BotsPosts
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param botId Database column bot_id SqlType(text)
    *  @param postId Database column post_id SqlType(int8)
    */
  case class BotsPostsRow(id: Long, botId: String, postId: Long)

  /** GetResult implicit for fetching BotsPostsRow objects using plain SQL queries */
  implicit def GetResultBotsPostsRow(implicit
    e0: GR[Long],
    e1: GR[String]
  ): GR[BotsPostsRow] = GR { prs =>
    import prs._
    BotsPostsRow.tupled((<<[Long], <<[String], <<[Long]))
  }

  /** Table description of table bots_posts. Objects of this class serve as prototypes for rows in queries. */
  class BotsPosts(_tableTag: Tag)
      extends profile.api.Table[BotsPostsRow](_tableTag, "bots_posts") {
    def * = (id, botId, postId) <> (BotsPostsRow.tupled, BotsPostsRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(botId), Rep.Some(postId))).shaped.<>(
      { r =>
        import r._; _1.map(_ => BotsPostsRow.tupled((_1.get, _2.get, _3.get)))
      },
      (_: Any) =>
        throw new Exception("Inserting into ? projection not supported.")
    )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    /** Database column bot_id SqlType(text) */
    val botId: Rep[String] = column[String]("bot_id")

    /** Database column post_id SqlType(int8) */
    val postId: Rep[Long] = column[Long]("post_id")

    /** Foreign key referencing Posts (database name bots_posts_post_id_fkey) */
    lazy val postsFk = foreignKey("bots_posts_post_id_fkey", postId, Posts)(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction
    )
  }

  /** Collection-like TableQuery object for table BotsPosts */
  lazy val BotsPosts = new TableQuery(tag => new BotsPosts(tag))
}
