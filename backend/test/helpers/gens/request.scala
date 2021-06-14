package helpers.gens

import adapters.controllers.application.{
  UninstallApplicationBody,
  UpdateClientInfoBody
}
import adapters.controllers.post.{CreatePostBody, DeletePostsBody}
import helpers.gens.string._
import org.scalacheck.Gen

object request extends RequestGen

trait RequestGen {
  val createPostBodyGen: Gen[CreatePostBody] = for {
    url         <- urlGen
    title       <- nonEmptyStringGen
    author      <- nonEmptyStringGen
    postedAt    <- Gen.posNum[Long]
    botIds      <- Gen.listOf(nonEmptyStringGen)
    testimonial <- Gen.option(nonEmptyStringGen)
  } yield CreatePostBody(url, title, author, postedAt, botIds, testimonial)

  val deletePostsBodyGen: Gen[DeletePostsBody] = for {
    ids <- Gen.nonEmptyListOf(Gen.posNum[Long])
  } yield DeletePostsBody(ids)

  val updateApplicationClientInfoBodyGen: Gen[UpdateClientInfoBody] = for {
    clientId     <- Gen.option(nonEmptyStringGen)
    clientSecret <- Gen.option(nonEmptyStringGen)
  } yield UpdateClientInfoBody(clientId, clientSecret)

  val uninstallBotBodyGen: Gen[UninstallApplicationBody] = for {
    teamId <- nonEmptyStringGen
    appId  <- nonEmptyStringGen
  } yield UninstallApplicationBody(teamId, appId)
}
