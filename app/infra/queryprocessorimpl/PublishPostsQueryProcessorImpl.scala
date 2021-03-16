package infra.queryprocessorimpl

import com.google.inject.Inject
import infra.dto.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import query.publishposts.{Post, PublishPostsQueryProcessor, PublishPostsView}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.API
import infra.syntax.all._

import scala.concurrent.{ExecutionContext, Future}

class PublishPostsQueryProcessorImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[PostgresProfile]
    with PublishPostsQueryProcessor with API {
  override def findAll(): Future[Seq[PublishPostsView]] = {
    val allAccessTokens = for {
      accessTokens <- AccessTokens.result
    } yield accessTokens.groupBy(_.botId).toSeq

    val currUnix      = System.currentTimeMillis / 1000
    val yesterdayUnix = currUnix - 3600 * 24
    val query         =
      for {
        accessTokens <- allAccessTokens
        posts        <- Posts
                          .join(BotsPosts)
                          .on(_.id === _.postId)
                          .filter { case (post, _) =>
                            post.createdAt > yesterdayUnix && post.createdAt <= currUnix
                          }
                          .result
      } yield for {
        (botId, tokens) <- accessTokens
        postView         = posts
                             .filter(_._2.botId == botId)
                             .map(_._1)
                             .map(p => Post(p.url, p.title))
        tokenRow        <- tokens
      } yield PublishPostsView(postView, tokenRow.token)

    db.run(query.transactionally)
  }.ifFailedThenToInfraError("error while PublishPostsQueryProcessor.findAll")
}
