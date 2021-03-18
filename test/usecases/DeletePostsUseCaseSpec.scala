package usecases

import domains.post.PostRepository
import helpers.traits.UseCaseSpec
import infra.DBError
import org.scalacheck.Gen
import usecases.DeletePostsUseCase.Params

import scala.concurrent.Future

class DeletePostsUseCaseSpec extends UseCaseSpec {
  val repo = mock[PostRepository]
  "exec" when {
    "succeed" should {
      "invoke PostRepository.delete once" in {
        forAll(Gen.nonEmptyListOf(postIdGen)) { ids =>
          val params = Params(ids)

          when(repo.delete(ids)).thenReturn(Future.unit)

          new DeletePostsUseCaseImpl(repo).exec(params).futureValue

          verify(repo, only).delete(ids)
          reset(repo)
        }
      }
    }

    "failed" should {
      "throw UseCaseError" in {
        forAll(Gen.nonEmptyListOf(postIdGen)) { ids =>
          val params = Params(ids)

          when(repo.delete(ids)).thenReturn(Future.failed(DBError("error")))

          val result = new DeletePostsUseCaseImpl(repo).exec(params)

          whenReady(result.failed) { e =>
            assert(
              e === SystemError(
                "error while postRepository.delete in delete posts use case" + "\n" + DBError(
                  "error"
                ).getMessage
              )
            )
          }
        }
      }
    }
  }
}
