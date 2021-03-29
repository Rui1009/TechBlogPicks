package controllers.bot

import adapters.controllers.bot.UninstallBotBody
import helpers.traits.ControllerSpec
import play.api.inject.bind
import io.circe.generic.auto._
import usecases.{SystemError, UninstallBotUseCase}
import play.api.inject._
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class BotControllerUninstallSpec extends ControllerSpec {
  val uc = mock[UninstallBotUseCase]

  override val app =
    builder.overrides(bind[UninstallBotUseCase].toInstance(uc)).build()

  "uninstall" when {
    "given body which is valid".which {
      "result succeed" should {
        "invoke use case exec once & return 201" in {
          forAll(uninstallBotBodyGen) { body =>
            when(uc.exec(*)).thenReturn(Future.unit)

            val path                = "/bot/uninstall"
            val res: Future[Result] =
              Request.post(path).withJsonBody(body).unsafeExec

            assert(status(res) === CREATED)
            verify(uc).exec(*)
            reset(uc)
          }
        }
      }

      "result failed" should {
        "return Internal Server Error" in {
          forAll(uninstallBotBodyGen) { body =>
            when(uc.exec(*)).thenReturn(Future.failed(SystemError("error")))

            val path                = "/bot/uninstall"
            val res: Future[Result] =
              Request.post(path).withJsonBody(body).unsafeExec

            assert(status(res) === INTERNAL_SERVER_ERROR)
            assert(
              decodeERes(res).unsafeGet.message == internalServerError + "error in BotController.uninstall\nSystemError\nerror"
            )
          }
        }
      }

    }
  }
}
