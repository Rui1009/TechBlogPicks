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

            val tezt =
              "[\"payload\" :[{\"type\":\"block_actions\",\"user\":{\"id\":\"U022GNFPV88\",\"username\":\"3116049c\",\"name\":\"3116049c\",\"team_id\":\"T021Z3M1DKN\"},\"api_app_id\":\"A01R7A9362Y\",\"token\":\"Du5klSVmg6m3jAhDnD8BaN3c\",\"container\":{\"type\":\"message\",\"message_ts\":\"1621203368.000900\",\"channel_id\":\"D021Z41KMFE\",\"is_ephemeral\":false},\"trigger_id\":\"2055875883063.2067123047668.5558162fb5621dfe80f72b601cba663a\",\"team\":{\"id\":\"T021Z3M1DKN\",\"domain\":\"winkietest2\"},\"enterprise\":null,\"is_enterprise_install\":false,\"channel\":{\"id\":\"D021Z41KMFE\",\"name\":\"directmessage\"},\"message\":{\"bot_id\":\"B021T5HDB3M\",\"type\":\"message\",\"text\":\"\\u3053\\u306e\\u30b3\\u30f3\\u30c6\\u30f3\\u30c4\\u306f\\u8868\\u793a\\u3067\\u304d\\u307e\\u305b\\u3093\\u3002\",\"user\":\"U0225HD1VTK\",\"ts\":\"1621203368.000900\",\"team\":\"T021Z3M1DKN\",\"blocks\":[{\"type\":\"section\",\"block_id\":\"gv9\",\"text\":{\"type\":\"mrkdwn\",\"text\":\"\\u30a4\\u30f3\\u30b9\\u30c8\\u30fc\\u30eb\\u3042\\u308a\\u304c\\u3068\\u3046\\u3054\\u3056\\u3044\\u307e\\u3059:hugging_face:\\nWinkie\\u306f\\u3042\\u306a\\u305f\\u306e\\u95a2\\u5fc3\\u306e\\u3042\\u308b\\u5206\\u91ce\\u306b\\u95a2\\u3059\\u308b\\u6700\\u65b0\\u306e\\u6280\\u8853\\u8a18\\u4e8b\\u3092\\u81ea\\u52d5\\u3067slack\\u4e0a\\u306b\\u5b9a\\u671f\\u914d\\u4fe1\\u3059\\u308b\\u30a2\\u30d7\\u30ea\\u3067\\u3059\\u3002\\n\\u3054\\u5229\\u7528\\u3044\\u305f\\u3060\\u304f\\u305f\\u3081\\u306b\\u3001\\u521d\\u3081\\u306b\\u30a2\\u30d7\\u30ea\\u3092\\u8ffd\\u52a0\\u3059\\u308b\\u30c1\\u30e3\\u30f3\\u30cd\\u30eb\\u3092\\u9078\\u629e\\u3057\\u3066\\u304f\\u3060\\u3055\\u3044\\u3002\",\"verbatim\":false}},{\"type\":\"actions\",\"block_id\":\"USb1\",\"elements\":[{\"type\":\"channels_select\",\"action_id\":\"actionId-0\",\"placeholder\":{\"type\":\"plain_text\",\"text\":\"Select a channel\",\"emoji\":false}}]}]},\"state\":{\"values\":{\"USb1\":{\"actionId-0\":{\"type\":\"channels_select\",\"selected_channel\":\"C021SUA097C\"}}}},\"response_url\":\"https:\\/\\/hooks.slack.com\\/actions\\/T021Z3M1DKN\\/2083263187713\\/3tAbpPwhI8PjrcUVlV9iPDrU\",\"actions\":[{\"type\":\"channels_select\",\"action_id\":\"actionId-0\",\"block_id\":\"USb1\",\"selected_channel\":\"C021SUA097C\",\"action_ts\":\"1621233618.926988\"}]})]]"
            println(PJson.parse(tezt))
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
