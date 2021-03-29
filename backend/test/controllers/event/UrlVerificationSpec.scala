package controllers.event

import helpers.traits.ControllerSpec
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import play.api.test.Helpers._

trait UrlVerificationSpecContext {
  val path = "/events"

  case class UrlVerificationView(challenge: String)
}

class UrlVerificationSpec
    extends ControllerSpec with UrlVerificationSpecContext {
  "url_verification" when {
    "succeed" should {
      "return 200 & valid body" in {
        forAll(nonEmptyStringGen) { challenge =>
          val body = Json.obj(
            "type"      -> Json.fromString("url_verification"),
            "challenge" -> Json.fromString(challenge)
          )

          val res = Request.post(path).withJsonBody(body).unsafeExec

          assert(
            decode[UrlVerificationView](
              contentAsString(res)
            ).unsafeGet.challenge === challenge
          )
        }
      }
    }
  }
}
