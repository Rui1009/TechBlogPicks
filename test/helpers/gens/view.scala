package helpers.gens

import helpers.gens.string._
import query.publishposts.{Post, PublishPostsView}
import cats.syntax.option._
import org.scalacheck.Gen
import query.bots.BotsView

object view extends ViewGen

trait ViewGen extends PublishPostsViewGen with BotsViewGen

trait PublishPostsViewGen {
  val postViewGen: Gen[Post] = for {
    url   <- urlGen
    title <- nonEmptyStringGen
  } yield Post(url.some, title)

  val publishPostsViewGen: Gen[PublishPostsView] = for {
    token    <- nonEmptyStringGen
    posts    <- Gen.nonEmptyListOf(postViewGen)
    channels <- Gen.nonEmptyListOf(nonEmptyStringGen)
  } yield PublishPostsView(posts, token, channels)
}

trait BotsViewGen {
  val botsViewGen: Gen[BotsView] = for {
    id   <- nonEmptyStringGen
    name <- nonEmptyStringGen
  } yield BotsView(id, name)
}
