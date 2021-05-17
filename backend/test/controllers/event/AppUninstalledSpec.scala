package controllers.event

import helpers.traits.ControllerSpec
import io.circe.{Json, JsonObject}
import play.api.inject.bind
import usecases.{SystemError, UninstallApplicationUseCase}
import play.api.test.Helpers._

import scala.concurrent.Future

trait AppUninstalledSpecContext { this: ControllerSpec =>
  val uc = mock[UninstallApplicationUseCase]

  val path = "/events"

  override val app =
    builder.overrides(bind[UninstallApplicationUseCase].toInstance(uc)).build()
}

class AppUninstalledSpec extends ControllerSpec with AppUninstalledSpecContext {
  "app_uninstalled" when {
    "given body which is valid".which {
      "results succeed" should {
        "invoke use case exec once & return 201" in {
          forAll(nonEmptyStringGen, nonEmptyStringGen) { (teamId, apiAppId) =>
            when(uc.exec(*)).thenReturn(Future.unit)

            val body = Json.obj(
              "team_id"    -> Json.fromString(teamId),
              "api_app_id" -> Json.fromString(apiAppId),
              "type"       -> Json.fromString("event_callback"),
              "event"      -> Json.fromJsonObject(
                JsonObject("type" -> Json.fromString("app_uninstalled"))
              )
            )

            val res = Request.post(path).withJsonBody(body).unsafeExec

            assert(status(res) === CREATED)
            verify(uc).exec(*)
            reset(uc)
          }
        }
      }
      "results failed" should {
        "return Internal Server Error" in {
          forAll(nonEmptyStringGen, nonEmptyStringGen) { (teamId, apiAppId) =>
            when(uc.exec(*)).thenReturn(Future.failed(SystemError("error")))

            val body = Json.obj(
              "team_id"    -> Json.fromString(teamId),
              "api_app_id" -> Json.fromString(apiAppId),
              "type"       -> Json.fromString("event_callback"),
              "event"      -> Json.fromJsonObject(
                JsonObject("type" -> Json.fromString("app_uninstalled"))
              )
            )

            val res = Request.post(path).withJsonBody(body).unsafeExec

            assert(status(res) === INTERNAL_SERVER_ERROR)
            assert(
              decodeERes(res).unsafeGet.message === internalServerError + "error in EventController.appUninstalled\nSystemError\nerror"
            )
          }
        }
      }
    }

    "given body which is invalid".which {
      "teamId & applicationId are invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body = Json.obj(
            "team_id"    -> Json.fromString(""),
            "api_app_id" -> Json.fromString(""),
            "type"       -> Json.fromString("event_callback"),
            "event"      -> Json.fromJsonObject(
              JsonObject("type" -> Json.fromString("app_uninstalled"))
            )
          )

          val res = Request.post(path).withJsonBody(body).unsafeExec

          val msg = """
                |BadRequestError
                |EmptyStringError: WorkSpaceId is empty string
                |EmptyStringError: ApplicationId is empty string
                |""".stripMargin.trim

          assert(status(res) === BAD_REQUEST)
          assert(decodeERes(res).unsafeGet.message === msg)
        }
      }
    }
  }
}
