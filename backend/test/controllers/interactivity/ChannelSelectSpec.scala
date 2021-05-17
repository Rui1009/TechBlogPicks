package controllers.interactivity

import helpers.traits.ControllerSpec
import io.circe.{Json, JsonObject}
import usecases.{JoinChannelUseCase, SystemError}
import play.api.inject.bind
import play.api.test.Helpers._
import play.api.libs.json.{Json => PJson}

import scala.concurrent.Future

class ChannelSelectSpec extends ControllerSpec {
  val uc   = mock[JoinChannelUseCase]
  val path = "/interactivities"

  override val app =
    builder.overrides(bind[JoinChannelUseCase].toInstance(uc)).build()

  "channel select" when {
    "given body which is valid".which {
      "results succeed" should {
        "invoke use case exec once & return 201" in {
          forAll(nonEmptyStringGen) { str =>
            when(uc.exec(*)).thenReturn(Future.unit)

            val body2 = Json.fromValues(
              Seq(
                Json.fromJsonObject(
                  JsonObject(
                    "payload" -> Json.fromValues(
                      Seq(
                        Json.fromJsonObject(
                          JsonObject(
                            "api_app_id" -> Json.fromString(str),
                            "team"       -> Json.fromJsonObject(
                              JsonObject("id" -> Json.fromString(str))
                            ),
                            "actions"    -> Json.fromValues(
                              Seq(
                                Json.fromJsonObject(
                                  JsonObject(
                                    "type"             -> Json
                                      .fromString("channels_select"),
                                    "selected_channel" -> Json.fromString(str)
                                  )
                                )
                              )
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
            )

            val body = Json.fromJsonObject(
              JsonObject(
                "payload" -> Json.fromValues(
                  Seq(
                    Json.fromJsonObject(
                      JsonObject(
                        "api_app_id" -> Json.fromString(str),
                        "team"       -> Json.fromJsonObject(
                          JsonObject("id" -> Json.fromString(str))
                        ),
                        "actions"    -> Json.fromValues(
                          Seq(
                            Json.fromJsonObject(
                              JsonObject(
                                "type"             -> Json.fromString("channels_select"),
                                "selected_channel" -> Json.fromString(str)
                              )
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
            )
            val resp = Request.post(path).withJsonBody(body2).unsafeExec

            println(body2)
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

            val body = Json.fromJsonObject(
              JsonObject(
                "payload" -> Json.fromValues(
                  Seq(
                    Json.fromJsonObject(
                      JsonObject(
                        "api_app_id" -> Json.fromString(str),
                        "team"       -> Json.fromJsonObject(
                          JsonObject("id" -> Json.fromString(str))
                        ),
                        "actions"    -> Json.fromValues(
                          Seq(
                            Json.fromJsonObject(
                              JsonObject(
                                "type"             -> Json.fromString("channels_select"),
                                "selected_channel" -> Json.fromString(str)
                              )
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
            )

            val resp = Request.post(path).withJsonBody(body).unsafeExec

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

          val body = Json.fromJsonObject(
            JsonObject(
              "payload" -> Json.fromValues(
                Seq(
                  Json.fromJsonObject(
                    JsonObject(
                      "api_app_id" -> Json.fromString(""),
                      "team"       -> Json.fromJsonObject(
                        JsonObject("id" -> Json.fromString("teamId"))
                      ),
                      "actions"    -> Json.fromValues(
                        Seq(
                          Json.fromJsonObject(
                            JsonObject(
                              "type"             -> Json.fromString("channels_select"),
                              "selected_channel" -> Json.fromString("channelId")
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
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

      "teamId is invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body = Json.fromJsonObject(
            JsonObject(
              "payload" -> Json.fromValues(
                Seq(
                  Json.fromJsonObject(
                    JsonObject(
                      "api_app_id" -> Json.fromString("appId"),
                      "team"       -> Json.fromJsonObject(
                        JsonObject("id" -> Json.fromString(""))
                      ),
                      "actions"    -> Json.fromValues(
                        Seq(
                          Json.fromJsonObject(
                            JsonObject(
                              "type"             -> Json.fromString("channels_select"),
                              "selected_channel" -> Json.fromString("channelId")
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
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

      "selected_channel is invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body = Json.fromJsonObject(
            JsonObject(
              "payload" -> Json.fromValues(
                Seq(
                  Json.fromJsonObject(
                    JsonObject(
                      "api_app_id" -> Json.fromString("appId"),
                      "team"       -> Json.fromJsonObject(
                        JsonObject("id" -> Json.fromString("teamId"))
                      ),
                      "actions"    -> Json.fromValues(
                        Seq(
                          Json.fromJsonObject(
                            JsonObject(
                              "type"             -> Json.fromString("channels_select"),
                              "selected_channel" -> Json.fromString("")
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
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

      "all params are invalid" should {
        "return Bad Request Error" in {
          when(uc.exec(*)).thenReturn(Future.unit)

          val body = Json.fromJsonObject(
            JsonObject(
              "payload" -> Json.fromValues(
                Seq(
                  Json.fromJsonObject(
                    JsonObject(
                      "api_app_id" -> Json.fromString(""),
                      "team"       -> Json.fromJsonObject(
                        JsonObject("id" -> Json.fromString(""))
                      ),
                      "actions"    -> Json.fromValues(
                        Seq(
                          Json.fromJsonObject(
                            JsonObject(
                              "type"             -> Json.fromString("channels_select"),
                              "selected_channel" -> Json.fromString("")
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
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
