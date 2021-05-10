package controllers.post

import adapters.controllers.post.CreatePostBody
import helpers.traits.ControllerSpec
import io.circe.generic.auto._
import play.api.inject._
import play.api.test.Helpers._
import usecases.{RegisterPostUseCase, SystemError}

import scala.concurrent.Future

trait CreateSpecContext { this: ControllerSpec =>
  val path = "/posts"

  val uc = mock[RegisterPostUseCase]

  override val app =
    builder.overrides(bind[RegisterPostUseCase].toInstance(uc)).build()

  val failedError =
    internalServerError + "error in PostController.create\nSystemError\nerror"
}

class PostControllerCreateSpec extends ControllerSpec with CreateSpecContext {
  "create" when {
    "given body which is valid, ".which {
      "results succeed" should {
        "invoke use case exec once & return 201 & valid body" in {
          forAll(createPostBodyGen) { body =>
            when(uc.exec(*)).thenReturn(Future.unit)

            val res = Request.post(path).withJsonBody(body).unsafeExec

            assert(status(res) === CREATED)
            assert(decodeRes[Unit](res).unsafeGet === Response[Unit](()))
            verify(uc).exec(*)
            reset(uc)
          }
        }
      }

      "results failed" should {
        "return Internal Server Error" in {
          forAll(createPostBodyGen) { body =>
            when(uc.exec(*)).thenReturn(Future.failed(SystemError("error")))

            val res = Request.post(path).withJsonBody(body).unsafeExec

            assert(status(res) === INTERNAL_SERVER_ERROR)
            assert(decodeERes(res).unsafeGet.message === failedError)
          }
        }
      }
    }

    "given body".which {
      "url is invalid" should {
        "return BadRequest Error" in {
          forAll(createPostBodyGen) { body =>
            val req = body.copy(url = "url")
            val res = Request.post(path).withJsonBody(req).unsafeExec

            assert(status(res) === BAD_REQUEST)
            assert(
              decodeERes(
                res
              ).unsafeGet.message === (badRequestError + urlError).trim
            )
          }
        }
      }

      "title is invalid" should {
        "return BadRequest Error" in {
          forAll(createPostBodyGen) { body =>
            val req = body.copy(title = "")
            val res = Request.post(path).withJsonBody(req).unsafeExec

            assert(status(res) === BAD_REQUEST)
            assert(
              decodeERes(
                res
              ).unsafeGet.message === badRequestError + emptyStringError(
                "PostTitle"
              ).trim
            )
          }
        }
      }

      "author is invalid" should {
        "return BadRequest Error" in {
          forAll(createPostBodyGen) { body =>
            val req = body.copy(author = "")
            val res = Request.post(path).withJsonBody(req).unsafeExec

            assert(status(res) === BAD_REQUEST)
            assert(
              decodeERes(
                res
              ).unsafeGet.message === badRequestError + emptyStringError(
                "PostAuthor"
              ).trim
            )
          }
        }
      }

      "postedAt is invalid" should {
        "return BadRequest Error" in {
          forAll(createPostBodyGen) { body =>
            val req = body.copy(postedAt = -1)
            val res = Request.post(path).withJsonBody(req).unsafeExec

            assert(status(res) === BAD_REQUEST)
            assert(
              decodeERes(
                res
              ).unsafeGet.message === badRequestError + negativeNumberError(
                "PostPostedAt"
              ).trim
            )
          }
        }
      }

      "applicationIds is invalid" should {
        "return BadRequest Error" in {
          forAll(createPostBodyGen) { body =>
            val req = body.copy(applicationIds = Seq(""))
            val res = Request.post(path).withJsonBody(req).unsafeExec

            assert(status(res) === BAD_REQUEST)
            assert(
              decodeERes(
                res
              ).unsafeGet.message === badRequestError + emptyStringError(
                "ApplicationId"
              ).trim
            )
          }
        }
      }

      "content is invalid at all" should {
        "return BadRequest Error" in {
          val req = CreatePostBody("", "", "", -1, Seq(""))
          val res = Request.post(path).withJsonBody(req).unsafeExec

          assert(status(res) === BAD_REQUEST)
          assert(
            decodeERes(res).unsafeGet.message === (badRequestError +
              urlError + emptyStringError("PostTitle") + emptyStringError(
                "PostAuthor"
              ) + negativeNumberError("PostPostedAt") + emptyStringError(
                "ApplicationId"
              )).trim
          )
        }
      }
    }
  }
}
