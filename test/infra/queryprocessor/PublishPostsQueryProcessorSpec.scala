package infra.queryprocessor

import helpers.traits.{HasDB, QueryProcessorSpec}
import infra.dto.Tables._
import query.publishposts.{Post, PublishPostsQueryProcessor, PublishPostsView}
import cats.syntax.option._
import org.scalatest._

trait PublishPostsQueryProcessorSpecContext { this: HasDB =>
  val currUnix = System.currentTimeMillis / 1000

  val beforeAction = DBIO
    .seq(
      AccessTokens.forceInsertAll(
        Seq(
          AccessTokensRow("token1", "bot1"),
          AccessTokensRow("token2", "bot1"),
          AccessTokensRow("token3", "bot2")
        )
      ),
      Posts.forceInsertAll(
        Seq(
          PostsRow(1, "url1".some, "title1", "rui1", 1, currUnix),
          PostsRow(2, "url2".some, "title2", "rui1", 1, currUnix),
          PostsRow(3, "url3".some, "title3", "rui1", 1, currUnix),
          PostsRow(4, "url4".some, "title4", "rui1", 1, currUnix - 3600 * 24)
        )
      ),
      BotsPosts.forceInsertAll(
        Seq(
          BotsPostsRow(1, "bot1", 1),
          BotsPostsRow(2, "bot1", 2),
          BotsPostsRow(3, "bot2", 1),
          BotsPostsRow(4, "bot2", 3),
          BotsPostsRow(5, "bot1", 4)
        )
      )
    )
    .transactionally

  val deleteAction = BotsPosts.delete >> Posts.delete >> AccessTokens.delete
}

class PublishPostsQueryProcessorSpec
    extends QueryProcessorSpec[PublishPostsQueryProcessor]
    with PublishPostsQueryProcessorSpecContext {
  before(db.run(beforeAction).futureValue)
  after(db.run(deleteAction).ready())

  "findAll" when {
    "success" should {
      "return PublishPostsView seq" in {
        val result   = queryProcessor.findAll().futureValue
        val expected = Seq(
          PublishPostsView(
            Seq(Post("url1".some, "title1"), Post("url2".some, "title2")),
            "token1"
          ),
          PublishPostsView(
            Seq(Post("url1".some, "title1"), Post("url2".some, "title2")),
            "token2"
          ),
          PublishPostsView(
            Seq(Post("url1".some, "title1"), Post("url3".some, "title3")),
            "token3"
          )
        )

        expected.foreach(v => assert(result.contains(v)))
        assert(result.length === expected.length)
      }
    }
  }
}
