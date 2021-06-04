package infra.queryprocessorimpl

import com.google.inject.Inject
import infra.dao.slack.UsersDao
import infra.dto.Tables._
import infra.syntax.all._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import query.publishposts._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API

import scala.concurrent.{ExecutionContext, Future}

class PublishPostsQueryProcessorImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  protected val usersDao: UsersDao
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile]
    with PublishPostsQueryProcessor with API {
  override def findAll(): Future[Seq[PublishPostsView]] = {
    val currUnix      = System.currentTimeMillis / 1000
    val yesterdayUnix = currUnix - 3600 * 24

    val workSpaceQ = for {
      workSpaces <- WorkSpaces.result
    } yield workSpaces.groupBy(_.botId).toSeq

    val postsQ = Posts
      .join(BotsPosts)
      .on(_.id === _.postId)
      .filter { case (post, _) =>
        post.createdAt > yesterdayUnix && post.createdAt <= currUnix
      }
      .result

    val query =
      for {
        workSpaces <- workSpaceQ
        posts      <- postsQ
      } yield for {
        (applicationId, tokens) <- workSpaces
        postView                 = posts
                                     .filter(_._2.botId == applicationId)
                                     .map(_._1)
                                     .map(p => Post(p.url, p.title, p.testimonial))
        tokenRow                <- tokens
      } yield for {
        conversations <-
          usersDao.conversations(tokenRow.token, "public_channel")
      } yield PublishPostsView(
        postView,
        tokenRow.token,
        conversations.channels.map(_.id)
      )

    db.run(query.transactionally).map(Future.sequence(_)).flatten
  }.ifFailedThenToInfraError("error while PublishPostsQueryProcessor.findAll")
}
