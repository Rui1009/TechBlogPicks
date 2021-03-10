package usecases

import domains.post.{Post, PostRepository}
import helpers.traits.UseCaseSpec
import usecases.RegisterPostUseCase._
import cats.syntax.option._
import infra.DBError

import scala.concurrent.Future

class RegisterPostUseCaseSpec extends UseCaseSpec {
  "exec" when {
    "succeed" should {
      "invoked PostRepository.add once" in {
        val repo = mock[PostRepository]
        forAll(postUrlGen, postTitleGen, postPostedAtGen) {
          (url, title, postedAt) =>
            val params = Params(url.some, title, postedAt)
            val post = Post(None, url.some, title, postedAt)

            when(repo.add(post))
              .thenReturn(Future.unit)

            new RegisterPostUseCaseImpl(repo).exec(params)

            verify(repo, only).add(post)
            reset(repo)
        }
      }
    }

    "failed" should {
      "throw UseCaseError" in {
        val repo = mock[PostRepository]
        forAll(postUrlGen, postTitleGen, postPostedAtGen) {
          (url, title, postedAt) =>
            val params = Params(url.some, title, postedAt)
            val post = Post(None, url.some, title, postedAt)

            when(repo.add(post))
              .thenReturn(Future.failed(DBError("error")))

            val result = new RegisterPostUseCaseImpl(repo).exec(params)

            whenReady(result.failed) { e =>
              assert(
                e == SystemError(
                  "error while postRepository.add in register post use case"
                    + DBError("error").getMessage
                )
              )
            }
        }
      }
    }
  }
}
