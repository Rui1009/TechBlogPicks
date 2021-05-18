package controllers.event

import helpers.traits.ControllerSpec
import io.circe.Json
import usecases.{GreetInInvitedChannelUseCase, SystemError}
import play.api.inject.bind
import play.api.test.Helpers._

import scala.concurrent.Future

class MemberJoinedChannelSpec extends ControllerSpec {

  val uc   = mock[GreetInInvitedChannelUseCase]
  val path = "/events"

  override val app =
    builder.overrides(bind[GreetInInvitedChannelUseCase].toInstance(uc)).build()

  "member_joined_channel" when {
    "given body which is valid".which {
      "results succeed" should {
        "invoke use case exec once & return 201" in {
          forAll(nonEmptyStringGen) { str =>
            when(uc.exec(*)).thenReturn(Future.unit)

            val body = Json.obj(
              "team_id"    -> Json.fromString(str),
              "api_app_id" -> Json.fromString(str),
              "event"      -> Json.obj(
                "type"    -> Json.fromString("member_joined_channel"),
                "channel" -> Json.fromString(str)
              )
            )
            val resp = Request.post(path).withJsonBody(body).unsafeExec

            assert(status(resp) === CREATED)
            verify(uc).exec(*)
            reset(uc)
          }
        }
      }

      "results failed" should {
        "return Internal Server Error" in {
          forAll(nonEmptyStringGen) { str =>
            when(uc.exec(*)).thenReturn(Future.failed(SystemError("error")))

            val body = Json.obj(
              "team_id"    -> Json.fromString(str),
              "api_app_id" -> Json.fromString(str),
              "event"      -> Json.obj(
                "type"    -> Json.fromString("member_joined_channel"),
                "channel" -> Json.fromString(str)
              )
            )
            val resp = Request.post(path).withJsonBody(body).unsafeExec

            assert(status(resp) === INTERNAL_SERVER_ERROR)
            assert(
              decodeERes(resp).unsafeGet.message === internalServerError + "error in EventController.memberJoinedChannel\nSystemError\nerror"
            )

          }
        }
      }
    }

    "given body which is invalid".which {
      "ChannelId is invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body = Json.obj(
            "team_id"    -> Json.fromString("team_id"),
            "api_app_id" -> Json.fromString("app_id"),
            "event"      -> Json.obj(
              "type"    -> Json.fromString("member_joined_channel"),
              "channel" -> Json.fromString("")
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
            "team_id"    -> Json.fromString("team_id"),
            "api_app_id" -> Json.fromString(""),
            "event"      -> Json.obj(
              "type"    -> Json.fromString("member_joined_channel"),
              "channel" -> Json.fromString("channel_id")
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

      "WorkSpaceId is invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body = Json.obj(
            "team_id"    -> Json.fromString(""),
            "api_app_id" -> Json.fromString("app_id"),
            "event"      -> Json.obj(
              "type"    -> Json.fromString("member_joined_channel"),
              "channel" -> Json.fromString("channel_id")
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
              "type"    -> Json.fromString("member_joined_channel"),
              "channel" -> Json.fromString("")
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
