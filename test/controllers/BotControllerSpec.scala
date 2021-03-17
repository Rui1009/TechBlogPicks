package controllers

import helpers.traits.ControllerSpec
import usecases.InstallBotUseCase
import play.api.inject._
import io.circe.generic.auto._
import play.api.test.Helpers._
import play.api.http.Status.OK

import scala.concurrent.Future

trait BotControllerSpecContent {
  this: ControllerSpec =>

  val uc = mock[InstallBotUseCase]

  override val app =
    builder.overrides(bind[InstallBotUseCase].toInstance(uc)).build()
}

class BotControllerSpec extends ControllerSpec with BotControllerSpecContent {
  "install" when {
    "given body which is valid, ".which {
      "results succeed" should {
        "invoke use case exec once & return 200 & valid body" in {
          forAll(nonEmptyStringGen, nonEmptyStringGen) { (code, botId) =>
            when(uc.exec(*)).thenReturn(Future.unit)
            val path = "/bot?code=" + code + "&bot_id=" + botId
            val res = Request.get(path).unsafeExec

            assert(status(res) === OK)
          }
        }
      }
    }
  }
}

