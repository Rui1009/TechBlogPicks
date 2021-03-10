package helpers.gens

import domains.accesstokenpublisher.AccessTokenPublisher
import domains.accesstokenpublisher.AccessTokenPublisher.AccessTokenPublisherToken
import domains.bot.Bot
import domains.bot.Bot.{BotId, BotName}
import domains.post.Post.PostId
import org.scalacheck.Gen
import helpers.gens.string._
import helpers.gens.number._

object domain extends DomainGen

trait DomainGen extends AccessTokenPublisherGen with BotGen

trait AccessTokenPublisherGen {
  val accessTokenGen: Gen[AccessTokenPublisherToken] =
    stringRefinedNonEmptyGen.map(AccessTokenPublisherToken(_))

  val accessTokenPublisherGen: Gen[AccessTokenPublisher] =
    accessTokenGen.map(AccessTokenPublisher(_))
}

trait BotGen {
  val accessTokenGen: Gen[AccessTokenPublisherToken] =
    stringRefinedNonEmptyGen.map(AccessTokenPublisherToken(_))

  val postIdGen: Gen[PostId] =
    longRefinedPositiveGen.map(PostId(_))

  val BotIdGen: Gen[BotId] =
    stringRefinedNonEmptyGen.map(BotId(_))

  val BotNameGen: Gen[BotName] =
    stringRefinedNonEmptyGen.map(BotName(_))

  val accessTokensGen: Gen[Seq[AccessTokenPublisherToken]] =
    Gen.listOfN(10, accessTokenGen)

  val postsGen: Gen[Seq[PostId]] =
    Gen.listOfN(10, postIdGen)

  val botGen: Gen[Bot] =
    

}
