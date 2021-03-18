package controllers.bot

import helpers.traits.ControllerSpec
import infra.DBError
import org.scalacheck.Gen
import query.bots.{BotsQueryProcessor, BotsView}
import play.api.inject._
import play.api.test.Helpers._
import io.circe.generic.auto._

import scala.concurrent.Future

trait BotControllerIndexSpecContext { this: ControllerSpec =>
  val qp = mock[BotsQueryProcessor]

  val path = "/bots"

  override val app =
    builder.overrides(bind[BotsQueryProcessor].toInstance(qp)).build()

  val failedError = """
      |InternalServerError
      |error in BotController.index
      |DBError
      |error
      |""".stripMargin.trim
}

class BotControllerIndexSpec
    extends ControllerSpec with BotControllerIndexSpecContext {
  "index" when {
    "succeed" should {
      "invoke BotsQueryProcessor.forAll once & return 200 & valid body" in {
        forAll(Gen.nonEmptyListOf(botsViewGen)) { views =>
          when(qp.findAll).thenReturn(Future.successful(views))

          val res  = Request.get(path).unsafeExec
          val body = decodeRes[Seq[BotsView]](res)

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
