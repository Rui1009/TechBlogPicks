package controllers

import adapters.controllers.post.CreatePostBody
import helpers.traits.ControllerSpec
import usecases.{RegisterPostUseCase, SystemError}
import cats.syntax.option._
import io.circe.generic.auto._
import play.api.inject._
import play.api.test.Helpers._

import scala.concurrent.Future

trait PostControllerSpecContext { this: ControllerSpec =>
  val path = "/posts"

  val uc = mock[RegisterPostUseCase]

  override val app =
    builder.overrides(bind[RegisterPostUseCase].toInstance(uc)).build()

  val failedError =
    internalServerError + "\nerror in PostController.create\nSystemError\nerror"
}

class PostControllerSpec extends ControllerSpec with PostControllerSpecContext {
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
            val req = body.copy(url = "url".some)
            val res = Request.post(path).withJsonBody(req).unsafeExec

            assert(status(res) === BAD_REQUEST)
            assert(
              decodeERes(res).unsafeGet.message === badRequestError + urlError
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
              )
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
              )
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
              )
            )
          }
        }
      }

      "botIds is invalid" should {
        "return BadRequest Error" in {
          forAll(createPostBodyGen) { body =>
            val req = body.copy(botIds = Seq(""))
            val res = Request.post(path).withJsonBody(req).unsafeExec

            assert(status(res) === BAD_REQUEST)
            assert(
              decodeERes(
                res
              ).unsafeGet.message === badRequestError + emptyStringError(
                "BotId"
              )
            )
          }
        }
      }

      "content is invalid at all" should {
        "return BadRequest Error" in {
          val req = CreatePostBody("".some, "", "", -1, Seq(""))
          val res = Request.post(path).withJsonBody(req).unsafeExec

          assert(status(res) === BAD_REQUEST)
          assert(
            decodeERes(res).unsafeGet.message === badRequestError +
              urlError + emptyStringError("PostTitle") + emptyStringError(
                "PostAuthor"
              ) + negativeNumberError("PostPostedAt") + emptyStringError(
                "BotId"
              )
          )
        }
      }
    }
  }
}
