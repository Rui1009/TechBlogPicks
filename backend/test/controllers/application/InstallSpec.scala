package controllers.application

import helpers.traits.ControllerSpec
import play.api.inject._
import play.api.test.Helpers._
import usecases.{InstallApplicationUseCase, SystemError}

import scala.concurrent.Future

trait ApplicationControllerInstallSpecContent {
  this: ControllerSpec =>

  val uc = mock[InstallApplicationUseCase]

  override val app =
    builder.overrides(bind[InstallApplicationUseCase].toInstance(uc)).build()
}

class ApplicationControllerInstallSpec
    extends ControllerSpec with ApplicationControllerInstallSpecContent {
  "install" when {
    "given body which is valid, ".which {
      "results succeed" should {
        "invoke use case exec once & return 200 & valid body" in {
          forAll(nonEmptyStringGen, nonEmptyStringGen) { (code, botId) =>
            when(uc.exec(*)).thenReturn(Future.unit)
            val path = "/bot?code=" + code + "&bot_id=" + botId
            val res  = Request.get(path).unsafeExec

            assert(redirectLocation(res) === Some("https://winkie.app/success"))
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
            val res  = Request.get(path).unsafeExec

            assert(redirectLocation(res) === Some("https://winkie.app/failure"))
            verify(uc).exec(*)
            reset(uc)
          }
        }
      }
    }

    "given body".which {
      "code is invalid" should {
        "return BadRequest Error" in {
          forAll(nonEmptyStringGen) { appId =>
            val path = "/bot?code=" + "&bot_id=" + appId
            val res  = Request.get(path).unsafeExec

            assert(status(res) === BAD_REQUEST)
            assert(
              decodeERes(
                res
              ).unsafeGet.message === (badRequestError + emptyStringError(
                "WorkSpaceTemporaryOauthCode"
              )).trim
            )
          }

        }
      }

      "api_app_id is invalid" should {
        "return BadRequest Error" in {
          forAll(nonEmptyStringGen) { code =>
            val path = "/bot?code=" + code + "&bot_id="
            val res  = Request.get(path).unsafeExec

            assert(status(res) === BAD_REQUEST)
            assert(
              decodeERes(
                res
              ).unsafeGet.message === (badRequestError + emptyStringError(
                "ApplicationId"
              )).trim
            )
          }
        }
      }

      "content is invalid at all" should {
        "return BadRequest Error" in {
          val path = "/bot?code=&bot_id="
          val res  = Request.get(path).unsafeExec

          assert(status(res) === BAD_REQUEST)
          assert(
            decodeERes(
              res
            ).unsafeGet.message === (badRequestError + emptyStringError(
              "WorkSpaceTemporaryOauthCode"
            ) + emptyStringError("ApplicationId")).trim
          )
        }
      }
    }
  }
}
