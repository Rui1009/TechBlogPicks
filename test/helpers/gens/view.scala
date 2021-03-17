package helpers.gens

import helpers.gens.string._
import query.publishposts.{Post, PublishPostsView}
import cats.syntax.option._
import org.scalacheck.Gen

object view extends ViewGen

trait ViewGen extends PublishPostsViewGen

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
