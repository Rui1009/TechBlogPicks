package infra.queryprocessor

import helpers.traits.QueryProcessorSpec
import query.posts.{PostsQueryProcessor, PostsView}
import infra.dto.Tables._

class PostQueryProcessorSpec extends QueryProcessorSpec[PostsQueryProcessor] {

  val beforeAction = DBIO.seq(
    Posts.forceInsertAll(
      Seq(
        PostsRow(1, Some("url1"), "Ningen Shikkaku", "Dazai Osamu", 1, 2),
        PostsRow(2, Some("url2"), "Kokoro", "Natsume Souseki", 2, 3),
        PostsRow(3, Some("url3"), "Maihime", "Mori Ougai", 3, 4),
        PostsRow(4, Some("url4"), "Takekurabe", "Higuchi ichiyou", 4, 1)
      )
    )
  )

  val deleteAction = Posts.delete

  before(db.run(beforeAction).futureValue)
  after(db.run(deleteAction).ready())

  "findAll" when {
    "success" should {
      "return PostsView seq" in {
        val result   = queryProcessor.findAll.futureValue
        val expected = Seq(
          PostsView(3, Some("url3"), "Maihime", "Mori Ougai", 3, 4),
          PostsView(2, Some("url2"), "Kokoro", "Natsume Souseki", 2, 3),
          PostsView(1, Some("url1"), "Ningen Shikkaku", "Dazai Osamu", 1, 2),
          PostsView(4, Some("url4"), "Takekurabe", "Higuchi ichiyou", 4, 1)
        )

        assert(result === expected)
      }
    }
  }
}
