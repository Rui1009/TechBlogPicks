package controllers.bot

import adapters.controllers.bot.UpdateClientInfoBody
import helpers.traits.ControllerSpec
import play.api.inject.bind
import usecases.{SystemError, UpdateApplicationClientInfoUseCase}
import io.circe.generic.auto._
import play.api.test.Helpers._

import scala.concurrent.Future

trait BotControllerUpdateSpecContext { this: ControllerSpec =>
  val uc = mock[UpdateApplicationClientInfoUseCase]

  val path = (id: String) => "/bots/" + id

  val failedError = """
      |InternalServerError
      |error in BotController.update
      |SystemError
      |error
      |""".stripMargin.trim

  override val app = builder
    .overrides(bind[UpdateApplicationClientInfoUseCase].toInstance(uc))
    .build()
}

class ApplicationControllerUpdateSpec
    extends ControllerSpec with BotControllerUpdateSpecContext {
  "update" when {
    "given body which is valid".which {
      "results succeed" should {
        "invoke use case exec once & return 201 & valid body" in {
          forAll(updateBotClientInfoBodyGen) { body =>
            when(uc.exec(*)).thenReturn(Future.unit)

            val res = Request.post(path("botId")).withJsonBody(body).unsafeExec

            assert(status(res) === CREATED)
            assert(decodeRes[Unit](res).unsafeGet === Response[Unit](()))
          }
        }
      }

      "results failed" should {
        "return Internal Server Error" in {
          forAll(updateBotClientInfoBodyGen) { body =>
            when(uc.exec(*)).thenReturn(Future.failed(SystemError("error")))

            val res = Request.post(path("botId")).withJsonBody(body).unsafeExec

            assert(status(res) === INTERNAL_SERVER_ERROR)
            assert(decodeERes(res).unsafeGet.message === failedError)
          }
        }
      }
    }

    "given body".which {
      "clientId is invalid" should {
        "return BadRequest Error" in {
          forAll(updateBotClientInfoBodyGen) { body =>
            val req = body.copy(clientId = Some(""))
            val res = Request.post(path("botId")).withJsonBody(req).unsafeExec

            assert(status(res) === BAD_REQUEST)
            assert(
              decodeERes(
                res
              ).unsafeGet.message === (badRequestError + emptyStringError(
                "BotClientId"
              )).trim
            )
          }
        }
      }

      "clientSecret is invalid" should {
        "return BadRequest Error" in {
          forAll(updateBotClientInfoBodyGen) { body =>
            val req = body.copy(clientSecret = Some(""))
            val res = Request.post(path("botId")).withJsonBody(req).unsafeExec

            assert(status(res) === BAD_REQUEST)
            assert(
              decodeERes(
                res
              ).unsafeGet.message === (badRequestError + emptyStringError(
                "BotClientSecret"
              )).trim
            )
          }
        }
      }

      "content is invalid at all" should {
        "return BadRequest Error" in {
          val req = UpdateClientInfoBody(Some(""), Some(""))
          val res = Request.post(path("botId")).withJsonBody(req).unsafeExec

          assert(status(res) === BAD_REQUEST)
          assert(
            decodeERes(
              res
            ).unsafeGet.message === (badRequestError + emptyStringError(
              "BotClientId"
            ) + emptyStringError("BotClientSecret")).trim
          )
        }
      }
    }
  }
}
