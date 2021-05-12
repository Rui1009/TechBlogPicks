package helpers.gens

import helpers.gens.string._
import helpers.gens.number._
import query.publishposts.{Post, PublishPostsView}
import cats.syntax.option._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import org.scalacheck.Gen
import query.applications.ApplicationsView
import query.posts.PostsView

object view extends ViewGen

trait ViewGen
    extends PublishPostsViewGen with ApplicationsViewGen with PostsViewGen

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

trait ApplicationsViewGen {
  val applicationsViewGen: Gen[ApplicationsView] = for {
    id           <- nonEmptyStringGen
    name         <- nonEmptyStringGen
    clientId     <- Gen.option(nonEmptyStringGen)
    clientSecret <- Gen.option(nonEmptyStringGen)
  } yield ApplicationsView(id, name, clientId, clientSecret)
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
