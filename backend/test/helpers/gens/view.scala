package helpers.gens

import helpers.gens.string._
import helpers.gens.number._
import query.publishposts.{Post, PublishPostsView}
import cats.syntax.option._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import org.scalacheck.Gen
import query.bots.BotsView
import query.posts.PostsView

object view extends ViewGen

trait ViewGen extends PublishPostsViewGen with BotsViewGen with PostsViewGen

trait PublishPostsViewGen {
  val postViewGen: Gen[Post] = for {
    url   <- urlGen
    title <- nonEmptyStringGen
  } yield Post(url, title)

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

trait PostsViewGen {
  val postsViewGen: Gen[PostsView] = for {
    id        <- positiveLongGen
    url       <- urlGen
    title     <- nonEmptyStringGen
    author    <- nonEmptyStringGen
    postedAt  <- positiveLongGen
    createdAt <- positiveLongGen
  } yield PostsView(id, url, title, author, postedAt, createdAt)
}
