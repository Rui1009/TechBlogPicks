package infra.queryprocessor

import helpers.traits.{HasDB, QueryProcessorSpec}
import infra.dto.Tables._
import query.publishposts.{Post, PublishPostsQueryProcessor, PublishPostsView}
import cats.syntax.option._
import mockws.MockWS
import mockws.MockWSHelpers.Action
import io.circe._
import org.scalatest.time.{Millis, Span}
import play.api.Application
import play.api.inject.bind
import play.api.libs.ws.WSClient
import play.api.mvc.Results.Ok

trait PublishPostsQueryProcessorSpecContext { this: HasDB =>
  val currUnix = System.currentTimeMillis / 1000

  val beforeAction = DBIO
    .seq(
      WorkSpaces.forceInsertAll(
        Seq(
          WorkSpacesRow("token1", "bot1", "team1"),
          WorkSpacesRow("token2", "bot1", "team2"),
          WorkSpacesRow("token3", "bot2", "team1")
        )
      ),
      Posts.forceInsertAll(
        Seq(
          PostsRow(1, "url1", "title1", "rui1", 1, currUnix),
          PostsRow(2, "url2", "title2", "rui1", 1, currUnix),
          PostsRow(3, "url3", "title3", "rui1", 1, currUnix),
          PostsRow(4, "url4", "title4", "rui1", 1, currUnix - 3600 * 24)
        )
      ),
      BotsPosts.forceInsertAll(
        Seq(
          BotsPostsRow(1, "bot1", 1),
          BotsPostsRow(2, "bot1", 2),
          BotsPostsRow(3, "bot2", 1),
          BotsPostsRow(4, "bot2", 3),
          BotsPostsRow(5, "bot1", 4)
        )
      )
    )
    .transactionally

  val deleteAction = BotsPosts.delete >> Posts.delete >> WorkSpaces.delete

  val channels = Seq("channel1", "channel2")

  val mockWs = MockWS {
    case ("GET", str: String)
        if str.matches("https://slack.com/api/users.conversations") =>
      val res = Json.fromJsonObject(
        JsonObject(
          "channels" ->
            Json.fromValues(
              channels.map(s =>
                Json.obj(
                  "id"   -> Json.fromString(s),
                  "name" -> Json.fromString("test")
                )
              )
            )
        )
      )
      Action(Ok(res.noSpaces))
  }
}

class PublishPostsQueryProcessorSpec
    extends QueryProcessorSpec[PublishPostsQueryProcessor]
    with PublishPostsQueryProcessorSpecContext {
  override val app: Application =
    builder.overrides(bind[WSClient].toInstance(mockWs)).build()

  implicit val conf: PatienceConfig =
    PatienceConfig(scaled(Span(10000, Millis)), scaled(Span(15, Millis)))

  "findAll" when {
    "success" should {
      "return PublishPostsView seq" in {

        db.run(deleteAction).futureValue
        db.run(beforeAction).futureValue
        val result   = queryProcessor.findAll().futureValue
        val expected = Seq(
          PublishPostsView(
            Seq(Post("url1", "title1"), Post("url2", "title2")),
            "token1",
            channels
          ),
          PublishPostsView(
            Seq(Post("url1", "title1"), Post("url2", "title2")),
            "token2",
            channels
          ),
          PublishPostsView(
            Seq(Post("url1", "title1"), Post("url3", "title3")),
            "token3",
            channels
          )
        )

        expected.foreach(v => assert(result.contains(v)))
        assert(result.length === expected.length)
      }
    }
  }
}
