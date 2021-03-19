package usecases

import domains.post.{Post, PostRepository}
import helpers.traits.UseCaseSpec
import usecases.RegisterPostUseCase._
import cats.syntax.option._
import infra.DBError
import org.scalacheck.Gen

import scala.concurrent.Future

class RegisterPostUseCaseSpec extends UseCaseSpec {
  "exec" when {
    "succeed" should {
      "invoke PostRepository.add once" in {
        val repo = mock[PostRepository]
        forAll(
          postUrlGen,
          postTitleGen,
          postAuthorGen,
          postPostedAtGen,
          Gen.listOf(botIdGen)
        ) { (url, title, author, postedAt, botIds) =>
          val params = Params(url.some, title, author, postedAt, botIds)
          val post   = Post(None, url.some, title, author, postedAt)

          when(repo.add(post, botIds)).thenReturn(Future.unit)

          new RegisterPostUseCaseImpl(repo).exec(params).futureValue

          verify(repo, only).add(post, botIds)
          reset(repo)
        }
      }
    }

    "failed" should {
      "throw UseCaseError" in {
        val repo = mock[PostRepository]
        forAll(
          postUrlGen,
          postTitleGen,
          postAuthorGen,
          postPostedAtGen,
          Gen.listOf(botIdGen)
        ) { (url, title, author, postedAt, botIds) =>
          val params = Params(url.some, title, author, postedAt, botIds)
          val post   = Post(None, url.some, title, author, postedAt)

          when(repo.add(post, botIds))
            .thenReturn(Future.failed(DBError("error")))

          val result = new RegisterPostUseCaseImpl(repo).exec(params)

          whenReady(result.failed) { e =>
            assert(
              e == SystemError(
                "error while postRepository.add in register post use case" +
                  "\n" + DBError("error").getMessage
              )
            )
          }
        }
      }
    }
  }
}
