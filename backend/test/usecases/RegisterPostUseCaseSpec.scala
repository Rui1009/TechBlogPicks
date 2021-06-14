package usecases

import domains.application.ApplicationRepository
import domains.post.{PostRepository, UnsavedPost}
import helpers.traits.UseCaseSpec
import infra.DBError
import org.scalacheck.Gen
import usecases.RegisterPostUseCase._

import scala.concurrent.Future

class RegisterPostUseCaseSpec extends UseCaseSpec {
  "exec" when {
    val postRepo        = mock[PostRepository]
    val applicationRepo = mock[ApplicationRepository]
    "succeed" should {
      "invoke PostRepository.add once" in {
        forAll(postGen, Gen.listOf(applicationIdGen), applicationGen) {
          (post, applicationIds, application) =>
            val params      = Params(
              post.url,
              post.title,
              post.author,
              post.postedAt,
              applicationIds,
              post.testimonial
            )
            val unsavedPost = UnsavedPost(
              post.url,
              post.title,
              post.author,
              post.postedAt,
              post.testimonial
            )

            when(postRepo.save(unsavedPost)).thenReturn(Future.successful(post))
            when(applicationRepo.filter(params.applicationIds))
              .thenReturn(Future.successful(Seq(application)))

            val assignedApplications = post.assign(Seq(application))

            when(applicationRepo.save(assignedApplications, post.id))
              .thenReturn(Future.successful(()))

            new RegisterPostUseCaseImpl(postRepo, applicationRepo)
              .exec(params)
              .futureValue

            verify(postRepo, only).save(unsavedPost)
            verify(applicationRepo).filter(params.applicationIds)
            verify(applicationRepo).save(assignedApplications, post.id)
            reset(postRepo)
            reset(applicationRepo)
        }
      }
    }

    "failed in postRepository.save" should {
      "throw use case error & applicationRepository.filter & applicationRepository.save not invoked" in {
        forAll(postGen, Gen.listOf(applicationIdGen)) {
          (post, applicationIds) =>
            val params      = Params(
              post.url,
              post.title,
              post.author,
              post.postedAt,
              applicationIds,
              post.testimonial
            )
            val unsavedPost = UnsavedPost(
              post.url,
              post.title,
              post.author,
              post.postedAt,
              post.testimonial
            )

            when(postRepo.save(unsavedPost))
              .thenReturn(Future.failed(DBError("error")))

            val result = new RegisterPostUseCaseImpl(postRepo, applicationRepo)
              .exec(params)

            val msg = """
                |SystemError
                |error while postRepository.save in register post use case
                |DBError
                |error
                |""".stripMargin.trim

            whenReady(result.failed)(e => assert(e.getMessage.trim === msg))
        }
      }
    }

    "no application exists in applicationRepository.filter" should {
      "throw use case error & not invoke applicationRepository.save" in {
        forAll(postGen, Gen.listOf(applicationIdGen)) {
          (post, applicationIds) =>
            val params      = Params(
              post.url,
              post.title,
              post.author,
              post.postedAt,
              applicationIds,
              post.testimonial
            )
            val unsavedPost = UnsavedPost(
              post.url,
              post.title,
              post.author,
              post.postedAt,
              post.testimonial
            )

            when(postRepo.save(unsavedPost)).thenReturn(Future.successful(post))
            when(applicationRepo.filter(params.applicationIds))
              .thenReturn(Future.successful(Seq()))

            val result = new RegisterPostUseCaseImpl(postRepo, applicationRepo)
              .exec(params)

            whenReady(result.failed) { e =>
              assert(
                e === NotFoundError(
                  "error while get applications in register post use case"
                )
              )
              verify(applicationRepo, times(0)).save(*, *)
            }
        }
      }
    }

    "failed in applicationRepository.save" should {
      "throw use case error" in {
        forAll(postGen, Gen.listOf(applicationIdGen), applicationGen) {
          (post, applicationIds, application) =>
            val params      = Params(
              post.url,
              post.title,
              post.author,
              post.postedAt,
              applicationIds,
              post.testimonial
            )
            val unsavedPost = UnsavedPost(
              post.url,
              post.title,
              post.author,
              post.postedAt,
              post.testimonial
            )

            when(postRepo.save(unsavedPost)).thenReturn(Future.successful(post))
            when(applicationRepo.filter(params.applicationIds))
              .thenReturn(Future.successful(Seq(application)))

            val assignedApplications = post.assign(Seq(application))

            when(applicationRepo.save(assignedApplications, post.id))
              .thenReturn(Future.failed(DBError("error")))

            val result = new RegisterPostUseCaseImpl(postRepo, applicationRepo)
              .exec(params)

            val msg = """
                |SystemError
                |error while applicationRepository.add in register post use case
                |DBError
                |error
                |""".stripMargin.trim

            whenReady(result.failed)(e => assert(e.getMessage.trim === msg))
        }
      }
    }
  }
}
