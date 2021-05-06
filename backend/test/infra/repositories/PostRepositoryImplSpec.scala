package infra.repositories

import helpers.traits.RepositorySpec
import infra.repositoryimpl.PostRepositoryImpl
import org.scalacheck.Gen
import infra.syntax.all._
import infra.dto.Tables._
import cats.syntax.option._
import domains.post.Post.PostId
import domains.post.UnsavedPost
import eu.timepit.refined.auto._

class PostRepositoryImplSpec extends RepositorySpec[PostRepositoryImpl] {
  "save" when {
    "succeed" should {
      "add new data".which {
        "length is right" in {
          forAll(postGen, Gen.listOf(botIdGen)) { (post, botIds) =>
            val unsavedPost =
              UnsavedPost(post.url, post.title, post.author, post.postedAt)
            repository.save(unsavedPost).futureValue
            val postLen     = db.run(Posts.length.result).futureValue
            assert(postLen === 1)
            db.run(Posts.delete).futureValue
          }
        }

        "values are right" in {
          forAll(postGen) { post =>
            val unsavedPost =
              UnsavedPost(post.url, post.title, post.author, post.postedAt)
            repository.save(unsavedPost).futureValue
            val postsRow    = db
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
            unsavedPost.toRow(1).shouldPartiallyEq(0, 5)(postsRow)
            db.run(Posts.delete).futureValue
          }
        }
      }
    }
  }

  "delete" when {
    "succeed" should {
      "delete data" in {
        val pre     = DBIO.seq(
          Posts.forceInsertAll(
            Seq(
              PostsRow(1, "url", "test", "rui", 1, 1),
              PostsRow(2, "url2", "test", "rui", 1, 1),
              PostsRow(3, "url3", "test", "rui", 1, 1)
            )
          ),
          BotsPosts.forceInsertAll(
            Seq(
              BotsPostsRow(1, "bot", 1),
              BotsPostsRow(2, "bot2", 2),
              BotsPostsRow(3, "bot3", 3)
            )
          )
        )
        val deleteQ = BotsPosts.delete >> Posts.delete

        db.run(pre.transactionally).futureValue

        val ids       = Seq(PostId(1L), PostId(2L), PostId(4L))
        repository.delete(ids).futureValue
        val posts     = db.run(Posts.result).futureValue
        val botsPosts = db.run(BotsPosts.result).futureValue

        assert(posts.length === 1)
        assert(posts.head.id === 3)
        assert(botsPosts.length === 1)
        assert(botsPosts.head.id === 3)

        db.run(deleteQ).futureValue
      }
    }
  }
}
