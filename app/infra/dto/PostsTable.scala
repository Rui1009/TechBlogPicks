package infra.dto
// AUTO-GENERATED Slick data model for table Posts
trait PostsTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Posts
   *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param url Database column url SqlType(text), Default(None)
   *  @param title Database column title SqlType(text)
   *  @param author Database column author SqlType(text)
   *  @param postedAt Database column posted_at SqlType(int8)
   *  @param createdAt Database column created_at SqlType(int8) */
  case class PostsRow(id: Long, url: Option[String] = None, title: String, author: String, postedAt: Long, createdAt: Long)
  /** GetResult implicit for fetching PostsRow objects using plain SQL queries */
  implicit def GetResultPostsRow(implicit e0: GR[Long], e1: GR[Option[String]], e2: GR[String]): GR[PostsRow] = GR{
    prs => import prs._
    PostsRow.tupled((<<[Long], <<?[String], <<[String], <<[String], <<[Long], <<[Long]))
  }
  /** Table description of table posts. Objects of this class serve as prototypes for rows in queries. */
  class Posts(_tableTag: Tag) extends profile.api.Table[PostsRow](_tableTag, "posts") {
    def * = (id, url, title, author, postedAt, createdAt) <> (PostsRow.tupled, PostsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), url, Rep.Some(title), Rep.Some(author), Rep.Some(postedAt), Rep.Some(createdAt))).shaped.<>({r=>import r._; _1.map(_=> PostsRow.tupled((_1.get, _2, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column url SqlType(text), Default(None) */
    val url: Rep[Option[String]] = column[Option[String]]("url", O.Default(None))
    /** Database column title SqlType(text) */
    val title: Rep[String] = column[String]("title")
    /** Database column author SqlType(text) */
    val author: Rep[String] = column[String]("author")
    /** Database column posted_at SqlType(int8) */
    val postedAt: Rep[Long] = column[Long]("posted_at")
    /** Database column created_at SqlType(int8) */
    val createdAt: Rep[Long] = column[Long]("created_at")
  }
  /** Collection-like TableQuery object for table Posts */
  lazy val Posts = new TableQuery(tag => new Posts(tag))
}
