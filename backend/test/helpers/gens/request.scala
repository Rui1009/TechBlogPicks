package helpers.gens

import adapters.controllers.post.{CreatePostBody, DeletePostsBody}
import org.scalacheck.Gen
import helpers.gens.string._
import helpers.gens.number._

object request extends RequestGen

trait RequestGen {
  val createPostBodyGen: Gen[CreatePostBody] = for {
    url      <- Gen.option(urlGen)
    title    <- nonEmptyStringGen
    author   <- nonEmptyStringGen
    postedAt <- Gen.posNum[Long]
    botIds   <- Gen.listOf(nonEmptyStringGen)
  } yield CreatePostBody(url, title, author, postedAt, botIds)

  val deletePostsBodyGen: Gen[DeletePostsBody] = for {
    ids <- Gen.nonEmptyListOf(Gen.posNum[Long])
  } yield DeletePostsBody(ids)
}
