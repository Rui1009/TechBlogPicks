package controllers.application

import helpers.traits.ControllerSpec
import infra.DBError
import org.scalacheck.Gen
import query.applications.{ApplicationsQueryProcessor, ApplicationsView}
import play.api.inject._
import play.api.test.Helpers._
import io.circe.generic.auto._

import scala.concurrent.Future

trait ApplicationControllerIndexSpecContext { this: ControllerSpec =>
  val qp = mock[ApplicationsQueryProcessor]

  val path = "/bots"

  override val app =
    builder.overrides(bind[ApplicationsQueryProcessor].toInstance(qp)).build()

  val failedError = """
      |InternalServerError
      |error in ApplicationController.index
      |DBError
      |error
      |""".stripMargin.trim
}

class ApplicationControllerIndexSpec
    extends ControllerSpec with ApplicationControllerIndexSpecContext {
  "index" when {
    "succeed" should {
      "invoke ApplicationsQueryProcessor.findAll once & return 200 & valid body" in {
        forAll(Gen.nonEmptyListOf(applicationsViewGen)) { views =>
          when(qp.findAll).thenReturn(Future.successful(views))

          val res  = Request.get(path).unsafeExec
          val body = decodeRes[Seq[ApplicationsView]](res)

          assert(status(res) === OK)
          assert(body.unsafeGet.data.length === views.length)
          views.foreach(view => assert(body.unsafeGet.data.contains(view)))
          verify(qp, only).findAll
          reset(qp)
        }
      }
    }

    "failed" should {
      "return Internal Server Error" in {
        when(qp.findAll).thenReturn(Future.failed(DBError("error")))

        val res = Request.get(path).unsafeExec

        assert(status(res) === INTERNAL_SERVER_ERROR)
        assert(decodeERes(res).unsafeGet.message === failedError)
      }
    }
  }
}
