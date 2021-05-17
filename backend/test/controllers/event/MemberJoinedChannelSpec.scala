package controllers.event

import helpers.traits.ControllerSpec
import io.circe.Json
import usecases.GreetInInvitedChannelUseCase
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
    }
  }
}
