package controllers.post

import helpers.traits.ControllerSpec
import infra.DBError
import io.circe.generic.auto._
import org.scalacheck.Gen
import play.api.inject._
import play.api.test.Helpers._
import query.posts.{PostsQueryProcessor, PostsView}

import scala.concurrent.Future

class PostControllerIndexSpec extends ControllerSpec {
  val qp = mock[PostsQueryProcessor]

  val path = "/posts"

  override val app =
    builder.overrides(bind[PostsQueryProcessor].toInstance(qp)).build()

  val failedError = """
                      |InternalServerError
                      |error in PostController.index
                      |DBError
                      |error
                      |""".stripMargin.trim

  "index" when {
    "succeed" should {
      "invoke PostsQueryProcessor.findAll once & return 200 & valid body" in {
        forAll(Gen.nonEmptyListOf(postsViewGen)) { view =>
          when(qp.findAll).thenReturn(Future.successful(view))

          val resp = Request.get(path).unsafeExec
          val body = decodeRes[Seq[PostsView]](resp)

          assert(status(resp) === OK)
          assert(body.unsafeGet.data.length === view.length)
          view.foreach(vi => assert(body.unsafeGet.data.contains(vi)))
          verify(qp, only).findAll
          reset(qp)
        }
      }
    }

    "failed" should {
      "return Internal Server Error" in {
        when(qp.findAll).thenReturn(Future.failed(DBError("error")))

        val resp = Request.get(path).unsafeExec

        assert(status(resp) === INTERNAL_SERVER_ERROR)
        assert(decodeERes(resp).unsafeGet.message === failedError)
      }
    }

  }
}
