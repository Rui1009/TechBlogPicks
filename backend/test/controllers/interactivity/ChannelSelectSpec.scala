package controllers.interactivity

import helpers.traits.ControllerSpec
import play.api.inject.bind
import play.api.libs.json.{Json => PJson}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import usecases.{JoinChannelUseCase, SystemError}

import scala.concurrent.Future

class ChannelSelectSpec extends ControllerSpec {
  val uc   = mock[JoinChannelUseCase]
  val path = "/interactivity"

  override val app =
    builder.overrides(bind[JoinChannelUseCase].toInstance(uc)).build()

  "channel select" when {
    "given body which is valid".which {
      "results succeed" should {
        "invoke use case exec once & return 201" in {
          forAll(nonEmptyStringGen) { str =>
            when(uc.exec(*)).thenReturn(Future.unit)

            val body =
              "AnyContentAsFormUrlEncoded(ListMap(payload->List({\"type\": \"block_actions\", \"api_app_id\": \"appId\", \"team\": {\"id\": \"teamId\"}, \"actions\": [{\"type\": \"channels_select\", \"selected_channel\": \"channelId\"}]})))"

            val resp = route(
              app,
              FakeRequest(POST, path).withJsonBody(
                PJson.parse(
                  body
                    .replace("AnyContentAsFormUrlEncoded(ListMap(", "[")
                    .replace("->List(", ":[")
                    .replace("[payload", "[{\"payload\"")
                    .dropRight(3) + "]}]"
                )
              )
            ).get

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

            val body =
              "AnyContentAsFormUrlEncoded(ListMap(payload->List({\"type\": \"block_actions\", \"api_app_id\": \"appId\", \"team\": {\"id\": \"teamId\"}, \"actions\": [{\"type\": \"channels_select\", \"selected_channel\": \"channelId\"}]})))"

            val resp = route(
              app,
              FakeRequest(POST, path).withJsonBody(
                PJson.parse(
                  body
                    .replace("AnyContentAsFormUrlEncoded(ListMap(", "[")
                    .replace("->List(", ":[")
                    .replace("[payload", "[{\"payload\"")
                    .dropRight(3) + "]}]"
                )
              )
            ).get

            assert(status(resp) === INTERNAL_SERVER_ERROR)
            assert(
              decodeERes(resp).unsafeGet.message === internalServerError + "error in InteractivityController.chanelSelect\nSystemError\nerror"
            )
          }
        }
      }
    }

    "given body which is invalid".which {
      "api_app_id is invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body =
            "AnyContentAsFormUrlEncoded(ListMap(payload->List({\"type\": \"block_actions\", \"api_app_id\": \"\", \"team\": {\"id\": \"teamId\"}, \"actions\": [{\"type\": \"channels_select\", \"selected_channel\": \"channelId\"}]})))"
          val resp = route(
            app,
            FakeRequest(POST, path).withJsonBody(
              PJson.parse(
                body
                  .replace("AnyContentAsFormUrlEncoded(ListMap(", "[")
                  .replace("->List(", ":[")
                  .replace("[payload", "[{\"payload\"")
                  .dropRight(3) + "]}]"
              )
            )
          ).get

          val msg = """
              |BadRequestError
              |EmptyStringError: ApplicationId is empty string
              |""".stripMargin.trim

          assert(status(resp) === BAD_REQUEST)
          assert(decodeERes(resp).unsafeGet.message === msg)

        }
      }

      "teamId is invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body =
            "AnyContentAsFormUrlEncoded(ListMap(payload->List({\"type\": \"block_actions\", \"api_app_id\": \"appId\", \"team\": {\"id\": \"\"}, \"actions\": [{\"type\": \"channels_select\", \"selected_channel\": \"channelId\"}]})))"
          val resp = route(
            app,
            FakeRequest(POST, path).withJsonBody(
              PJson.parse(
                body
                  .replace("AnyContentAsFormUrlEncoded(ListMap(", "[")
                  .replace("->List(", ":[")
                  .replace("[payload", "[{\"payload\"")
                  .dropRight(3) + "]}]"
              )
            )
          ).get

          val msg = """
              |BadRequestError
              |EmptyStringError: WorkSpaceId is empty string
              |""".stripMargin.trim

          assert(status(resp) === BAD_REQUEST)
          assert(decodeERes(resp).unsafeGet.message === msg)
        }
      }

      "selected_channel is invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body =
            "AnyContentAsFormUrlEncoded(ListMap(payload->List({\"type\": \"block_actions\", \"api_app_id\": \"appId\", \"team\": {\"id\": \"teamId\"}, \"actions\": [{\"type\": \"channels_select\", \"selected_channel\": \"\"}]})))"
          val resp = route(
            app,
            FakeRequest(POST, path).withJsonBody(
              PJson.parse(
                body
                  .replace("AnyContentAsFormUrlEncoded(ListMap(", "[")
                  .replace("->List(", ":[")
                  .replace("[payload", "[{\"payload\"")
                  .dropRight(3) + "]}]"
              )
            )
          ).get

          val msg = """
                      |BadRequestError
                      |EmptyStringError: ChannelId is empty string
                      |""".stripMargin.trim

          assert(status(resp) === BAD_REQUEST)
          assert(decodeERes(resp).unsafeGet.message === msg)
        }
      }

      "all params are invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body =
            "AnyContentAsFormUrlEncoded(ListMap(payload->List({\"type\": \"block_actions\", \"api_app_id\": \"\", \"team\": {\"id\": \"\"}, \"actions\": [{\"type\": \"channels_select\", \"selected_channel\": \"\"}]})))"
          val resp = route(
            app,
            FakeRequest(POST, path).withJsonBody(
              PJson.parse(
                body
                  .replace("AnyContentAsFormUrlEncoded(ListMap(", "[")
                  .replace("->List(", ":[")
                  .replace("[payload", "[{\"payload\"")
                  .dropRight(3) + "]}]"
              )
            )
          ).get

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
