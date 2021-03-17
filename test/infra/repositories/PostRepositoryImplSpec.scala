package infra.repositories

import helpers.traits.RepositorySpec
import infra.repositoryimpl.PostRepositoryImpl
import org.scalacheck.Gen
import infra.syntax.all._
import infra.dto.Tables._

class PostRepositoryImplSpec extends RepositorySpec[PostRepositoryImpl] {
  "add" when {
    "succeed" should {
      "add new data".which {
        "length is right" in {
          forAll(postGen, Gen.listOf(botIdGen)) { (post, botIds) =>
            repository.add(post, botIds).futureValue
            val postLen      = db.run(Posts.length.result).futureValue
            val botsPostsLen = db.run(BotsPosts.length.result).futureValue
            assert(postLen === 1)
            assert(botsPostsLen === botIds.length)
            db.run(BotsPosts.delete >> Posts.delete).futureValue
          }
        }

        "values are right" in {
          forAll(postGen, Gen.listOf(botIdGen)) { (post, botIds) =>
            repository.add(post, botIds).futureValue
            val postsRow      = db
              .run(Posts.result.head)
              .map(r =>
                PostsRow(
                  r.id,
                  r.url,
                  r.title,
                  r.author,
                  r.postedAt,
                  r.createdAt
                )
              )
              .futureValue
            val botsPostsRows = db.run(BotsPosts.result).futureValue
            post.toRow(1).shouldPartiallyEq(0, 5)(postsRow)
            botsPostsRows.foreach { row =>
              assert(botIds.map(_.value.value).contains(row.botId))
            }
            db.run(BotsPosts.delete >> Posts.delete).futureValue
          }
        }
      }
    }
  }
}
