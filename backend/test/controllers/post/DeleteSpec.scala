package controllers.post

import helpers.traits.ControllerSpec
import io.circe.generic.auto._
import play.api.inject._
import play.api.test.Helpers._
import usecases.DeletePostsUseCase

import scala.concurrent.Future

trait PostControllerDeleteSpecContext {
  this: ControllerSpec =>
  val uc = mock[DeletePostsUseCase]

  val path = "/posts"

  override val app =
    builder.overrides(bind[DeletePostsUseCase].toInstance(uc)).build()
}

class PostControllerDeleteSpec
    extends ControllerSpec with PostControllerDeleteSpecContext {
  "delete" when {
    "given body".which {
      "is valid" should {
        "invoke DeletePostsUseCase.exec once & return 200 & valid body" in {
          forAll(deletePostsBodyGen) { body =>
            when(uc.exec(*)).thenReturn(Future.unit)

            val res = Request.delete(path).withJsonBody(body).unsafeExec

            assert(status(res) === OK)
            assert(decodeRes[Unit](res).unsafeGet === Response[Unit](()))
            verify(uc).exec(*)
            reset(uc)
          }
        }
      }
      "is invalid" should {
        "return Internal Server Error" in {
          forAll(deletePostsBodyGen) { body =>
            val req = body.copy(ids = body.ids :+ -1L)

            val res = Request.delete(path).withJsonBody(req).unsafeExec

            val msg = """
                |BadRequestError
                |NegativeNumberError: PostId is negative number
                |""".stripMargin.trim

            assert(status(res) === BAD_REQUEST)
            assert(decodeERes(res).unsafeGet.message === msg)
          }
        }
      }
    }
  }
}
