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
              "team_id"    -> Json.fromString(str),
              "api_app_id" -> Json.fromString(str),
              "event"      -> Json.obj(
                "channel" -> Json.fromString(str),
                "user"    -> Json.fromString(str),
                "type"    -> Json.fromString("app_home_opened")
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
              "team_id"    -> Json.fromString(str),
              "api_app_id" -> Json.fromString(str),
              "event"      -> Json.obj(
                "channel" -> Json.fromString(str),
                "user"    -> Json.fromString(str),
                "type"    -> Json.fromString("app_home_opened")
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
            "team_id"    -> Json.fromString("teamId"),
            "api_app_id" -> Json.fromString("appID"),
            "event"      -> Json.obj(
              "channel" -> Json.fromString(""),
              "user"    -> Json.fromString("userId"),
              "type"    -> Json.fromString("app_home_opened")
            )
          )
          val resp = Request.post(path).withJsonBody(body).unsafeExec

          val msg = """
              |BadRequestError
              |EmptyStringError: ChannelId is empty string
              |""".stripMargin.trim

          assert(status(resp) === BAD_REQUEST)
          assert(decodeERes(resp).unsafeGet.message === msg)
        }
      }

      "ApplicationId is invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body = Json.obj(
            "team_id"    -> Json.fromString("teamId"),
            "api_app_id" -> Json.fromString(""),
            "event"      -> Json.obj(
              "channel" -> Json.fromString("channelId"),
              "user"    -> Json.fromString("userId"),
              "type"    -> Json.fromString("app_home_opened")
            )
          )
          val resp = Request.post(path).withJsonBody(body).unsafeExec

          val msg = """
                      |BadRequestError
                      |EmptyStringError: ApplicationId is empty string
                      |""".stripMargin.trim

          assert(status(resp) === BAD_REQUEST)
          assert(decodeERes(resp).unsafeGet.message === msg)
        }
      }

      "workSpaceId is invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body = Json.obj(
            "team_id"    -> Json.fromString(""),
            "api_app_id" -> Json.fromString("appId"),
            "event"      -> Json.obj(
              "channel" -> Json.fromString("channel"),
              "user"    -> Json.fromString("user"),
              "type"    -> Json.fromString("app_home_opened")
            )
          )
          val resp = Request.post(path).withJsonBody(body).unsafeExec

          val msg = """
                      |BadRequestError
                      |EmptyStringError: WorkSpaceId is empty string
                      |""".stripMargin.trim

          assert(status(resp) === BAD_REQUEST)
          assert(decodeERes(resp).unsafeGet.message === msg)
        }
      }

      "all params are invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body = Json.obj(
            "team_id"    -> Json.fromString(""),
            "api_app_id" -> Json.fromString(""),
            "event"      -> Json.obj(
              "channel" -> Json.fromString(""),
              "user"    -> Json.fromString(""),
              "type"    -> Json.fromString("app_home_opened")
            )
          )
          val resp = Request.post(path).withJsonBody(body).unsafeExec

          val msg = """
                      |BadRequestError
                      |EmptyStringError: ChannelId is empty string
                      |EmptyStringError: ApplicationId is empty string
                      |EmptyStringError: WorkSpaceId is empty string
                      |""".stripMargin.trim

          assert(status(resp) === BAD_REQUEST)
          assert(decodeERes(resp).unsafeGet.message === msg)
        }
      }
    }
  }
}
