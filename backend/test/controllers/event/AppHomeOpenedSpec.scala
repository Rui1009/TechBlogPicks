package controllers.event

import helpers.traits.ControllerSpec
import io.circe.Json
import play.api.Application
import usecases.{PostOnboardingMessageUseCase, SystemError}
import play.api.inject.bind
import play.api.test.Helpers._

import scala.concurrent.Future

class AppHomeOpenedSpec extends ControllerSpec {
  val uc   = mock[PostOnboardingMessageUseCase]
  val path = "/events"

  override val app: Application =
    builder.overrides(bind[PostOnboardingMessageUseCase].toInstance(uc)).build()

  "app_home_opened" when {
    "given body which is valid".which {
      "results succeed" should {
        "invoke use case exec once & return 201" in {
          forAll(nonEmptyStringGen) { str =>
            when(uc.exec(*)).thenReturn(Future.unit)

            val body = Json.obj(
              "channel" -> Json.fromString(str),
              "view"    -> Json.obj(
                "app_id"                -> Json.fromString(str),
                "app_installed_team_id" -> Json.fromString(str)
              )
            )

            val resp = Request.post(path).withJsonBody(body).unsafeExec
            assert(status(resp) === CREATED)
            verify(uc).exec(*)
            reset(uc)
          }
        }
      }

      "result failed" should {
        "return Internal Server Error" in {
          forAll(nonEmptyStringGen) { str =>
            when(uc.exec(*)).thenReturn(Future.failed(SystemError("error")))

            val body = Json.obj(
              "channel" -> Json.fromString(str),
              "view"    -> Json.obj(
                "app_id"                -> Json.fromString(str),
                "app_installed_team_id" -> Json.fromString(str)
              )
            )
            val resp = Request.post(path).withJsonBody(body).unsafeExec

            assert(status(resp) === INTERNAL_SERVER_ERROR)
            assert(
              decodeERes(resp).unsafeGet.message === internalServerError + "error in EventController.appHomeOpened\nSystemError\nerror"
            )
          }
        }
      }
    }

    "given body which is invalid".which {
      "channel is invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body = Json.obj(
            "channel" -> Json.fromString(""),
            "view"    -> Json.obj(
              "app_id"                -> Json.fromString("appId"),
              "app_installed_team_id" -> Json.fromString("teamId")
            )
          )
          val resp = Request.post(path).withJsonBody(body).unsafeExec

          val msg = """
              |BadRequestError
              |domains.EmptyStringError: MessageChannelId is empty string
              |""".stripMargin.trim

          assert(status(resp) === BAD_REQUEST)
          assert(decodeERes(resp).unsafeGet.message === msg)
        }
      }

      "botId is invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body = Json.obj(
            "channel" -> Json.fromString("channelId"),
            "view"    -> Json.obj(
              "app_id"                -> Json.fromString(""),
              "app_installed_team_id" -> Json.fromString("teamId")
            )
          )
          val resp = Request.post(path).withJsonBody(body).unsafeExec

          val msg = """
                      |BadRequestError
                      |domains.EmptyStringError: BotId is empty string
                      |""".stripMargin.trim

          assert(status(resp) === BAD_REQUEST)
          assert(decodeERes(resp).unsafeGet.message === msg)
        }
      }

      "workSpaceId is invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body = Json.obj(
            "channel" -> Json.fromString("channelId"),
            "view"    -> Json.obj(
              "app_id"                -> Json.fromString("botId"),
              "app_installed_team_id" -> Json.fromString("")
            )
          )
          val resp = Request.post(path).withJsonBody(body).unsafeExec

          val msg = """
                      |BadRequestError
                      |domains.EmptyStringError: WorkSpaceId is empty string
                      |""".stripMargin.trim

          assert(status(resp) === BAD_REQUEST)
          assert(decodeERes(resp).unsafeGet.message === msg)
        }
      }

      "all params are invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body = Json.obj(
            "channel" -> Json.fromString(""),
            "view"    -> Json.obj(
              "app_id"                -> Json.fromString(""),
              "app_installed_team_id" -> Json.fromString("")
            )
          )
          val resp = Request.post(path).withJsonBody(body).unsafeExec

          val msg = """
                      |BadRequestError
                      |domains.EmptyStringError: MessageChannelId is empty string
                      |domains.EmptyStringError: BotId is empty string
                      |domains.EmptyStringError: WorkSpaceId is empty string
                      |""".stripMargin.trim

          assert(status(resp) === BAD_REQUEST)
          assert(decodeERes(resp).unsafeGet.message === msg)
        }
      }
    }
  }
}
