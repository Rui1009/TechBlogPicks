package infra.queryprocessor

import helpers.tags.DBTest
import helpers.traits.QueryProcessorSpec
import infra.dto.Tables._
import query.posts.{PostsQueryProcessor, PostsView}

class PostQueryProcessorSpec extends QueryProcessorSpec[PostsQueryProcessor] {

  val beforeAction = DBIO.seq(
    Posts.forceInsertAll(
      Seq(
        PostsRow(1, "url1", "Ningen Shikkaku", "Dazai Osamu", 1, 2),
        PostsRow(2, "url2", "Kokoro", "Natsume Souseki", 2, 3),
        PostsRow(3, "url3", "Maihime", "Mori Ougai", 3, 4),
        PostsRow(4, "url4", "Takekurabe", "Higuchi ichiyou", 4, 1)
      )
    )
  )

  val deleteAction = BotsPosts.delete >> Posts.delete

  "findAll" when {
    "success" should {
      "return PostsView seq" taggedAs DBTest in {

        db.run(deleteAction).futureValue
        db.run(beforeAction).futureValue

        val result   = queryProcessor.findAll.futureValue
        val expected = Seq(
          PostsView(3, "url3", "Maihime", "Mori Ougai", 3, 4),
          PostsView(2, "url2", "Kokoro", "Natsume Souseki", 2, 3),
          PostsView(1, "url1", "Ningen Shikkaku", "Dazai Osamu", 1, 2),
          PostsView(4, "url4", "Takekurabe", "Higuchi ichiyou", 4, 1)
        )

        assert(result === expected)

        db.run(deleteAction).futureValue
      }
    }
  }
}
