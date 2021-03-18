package controllers.bot

import helpers.traits.ControllerSpec
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.inject._
import play.api.test.Helpers._
import usecases.{InstallBotUseCase, SystemError}

import scala.concurrent.Future

trait BotControllerInstallSpecContent {
  this: ControllerSpec =>

  val uc = mock[InstallBotUseCase]

  override val app =
    builder.overrides(bind[InstallBotUseCase].toInstance(uc)).build()

  val failedError =
    internalServerError + "error in BotController.install\nSystemError\nerror"
}

class BotControllerInstallSpec
    extends ControllerSpec
    with BotControllerInstallSpecContent {
  "install" when {
    "given body which is valid, ".which {
      "results succeed" should {
        "invoke use case exec once & return 200 & valid body" in {
          forAll(nonEmptyStringGen, nonEmptyStringGen) { (code, botId) =>
            when(uc.exec(*)).thenReturn(Future.unit)
            val path = "/bot?code=" + code + "&bot_id=" + botId
            val res = Request.get(path).unsafeExec

            assert(status(res) === OK)
            assert(decodeRes[Unit](res).unsafeGet === Response[Unit](()))
            verify(uc).exec(*)
            reset(uc)
          }
        }
      }

      "results failed" should {
        "return Internal Server Error" in {
          forAll(nonEmptyStringGen, nonEmptyStringGen) { (code, botId) =>
            when(uc.exec(*)).thenReturn(Future.failed(SystemError("error")))
            val path = "/bot?code=" + code + "&bot_id=" + botId
            val res = Request.get(path).unsafeExec

            assert(status(res) === INTERNAL_SERVER_ERROR)
            assert(decodeERes(res).unsafeGet.message === failedError)
          }
        }
      }
    }

    "given body".which {
      "code is invalid" should {
        "return BadRequest Error" in {
          forAll(nonEmptyStringGen) { botId =>
            val path = "/bot?code=" + "&bot_id=" + botId
            val res = Request.get(path).unsafeExec

            assert(status(res) === BAD_REQUEST)
            assert(
              decodeERes(
                res
              ).unsafeGet.message === (badRequestError + emptyStringError(
                "temporaryOauthCode"
              )).trim
            )
          }

        }
      }

      "bot_id is invalid" should {
        "return BadRequest Error" in {
          forAll(nonEmptyStringGen) { code =>
            val path = "/bot?code=" + code + "&bot_id="
            val res = Request.get(path).unsafeExec

            assert(status(res) === BAD_REQUEST)
            assert(
              decodeERes(
                res
              ).unsafeGet.message === (badRequestError + emptyStringError(
                "BotId"
              )).trim
            )
          }
        }
      }

      "content is invalid at all" should {
        "return BadRequest Error" in {
          val path = "/bot?code=&bot_id="
          val res = Request.get(path).unsafeExec

          assert(status(res) === BAD_REQUEST)
          assert(
            decodeERes(
              res
            ).unsafeGet.message === (badRequestError + emptyStringError(
              "temporaryOauthCode"
            ) + emptyStringError("BotId")).trim
          )
        }
      }
    }
  }
}
