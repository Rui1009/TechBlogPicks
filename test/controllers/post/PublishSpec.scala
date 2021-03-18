package controllers.post

import helpers.traits.ControllerSpec
import infra.DBError
import io.circe.Json
import mockws.MockWS
import query.publishposts.PublishPostsQueryProcessor
import play.api.test.Helpers._
import play.api.inject._
import play.api.mvc.Results.Ok
import mockws.MockWSHelpers.Action
import org.scalacheck.Gen
import play.api.libs.ws.WSClient

import scala.concurrent.Future

trait PublishSuccessSpecContext { this: ControllerSpec =>
  val path = "/posts/publish"

  val query = mock[PublishPostsQueryProcessor]

  val mockWs = MockWS {
    case ("POST", str: String)
        if str.matches("https://slack.com/api/chat.postMessage") =>
      val res = Json.obj(
        "ok"      -> Json.fromBoolean(true),
        "channel" -> Json.fromString("testId")
      )
      Action(Ok(res.noSpaces))
  }
}

class PostControllerPublishSuccessSpec
    extends ControllerSpec with PublishSuccessSpecContext {
  override val app = builder
    .overrides(
      bind[PublishPostsQueryProcessor].toInstance(query),
      bind[WSClient].toInstance(mockWs)
    )
    .build

  "publish" when {
    "succeed" should {
      "return Ok" in {
        forAll(Gen.nonEmptyListOf(publishPostsViewGen)) { views =>
          when(query.findAll()).thenReturn(Future.successful(views))

          val res = Request.get(path).unsafeExec

          assert(status(res) === OK)
          assert(decodeRes[Unit](res).unsafeGet === Response[Unit](()))
          verify(query).findAll()
          reset(query)
        }
      }
    }
  }
}

trait PublishFailedSpecContext { this: ControllerSpec =>
  val path = "/posts/publish"

  val query = mock[PublishPostsQueryProcessor]

  val mockWs = MockWS {
    case ("POST", str: String)
        if str.matches("https://slack.com/api/chat.postMessage") =>
      val res = Json.obj("ok" -> Json.fromBoolean(false))
      Action(Ok(res.noSpaces))
  }
}

class PostControllerPublishFailedSpec
    extends ControllerSpec with PublishFailedSpecContext {
  override val app = builder
    .overrides(
      bind[PublishPostsQueryProcessor].toInstance(query),
      bind[WSClient].toInstance(mockWs)
    )
    .build

  "publish" when {
    "failed post message" should {
      "return Internal Server Error" in {
        forAll(Gen.nonEmptyListOf(publishPostsViewGen).filter(_.nonEmpty)) {
          views =>
            when(query.findAll()).thenReturn(Future.successful(views))

            val res = Request.get(path).unsafeExec

            val msg = """InternalServerError
                |error in PostController.publish
                |APIError
                |post message failed
                |Attempt to decode value on failed cursor: DownField(channel)""".stripMargin

            assert(status(res) === INTERNAL_SERVER_ERROR)
            assert(decodeERes(res).unsafeGet.message === msg)
        }
      }
    }

    "findAll failed" should {
      "return Internal Server Error" in {
        when(query.findAll()).thenReturn(Future.failed(DBError("error")))

        val res = Request.get(path).unsafeExec

        val msg =
          "InternalServerError\nerror in PostController.publish\nDBError\nerror"

        assert(status(res) === INTERNAL_SERVER_ERROR)
        assert(decodeERes(res).unsafeGet.message === msg)
      }
    }
  }
}
