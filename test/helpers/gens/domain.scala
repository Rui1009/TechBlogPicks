package helpers.gens

import domains.accesstokenpublisher.AccessTokenPublisher
import domains.bot.Bot
import domains.bot.Bot.{BotId, BotName}
import domains.post.Post.PostId
import domains.accesstokenpublisher.AccessTokenPublisher.{AccessTokenPublisherTemporaryOauthCode, AccessTokenPublisherToken}
import org.scalacheck.Gen
import helpers.gens.string._
import helpers.gens.number._

object domain extends DomainGen

trait DomainGen extends AccessTokenPublisherGen with BotGen with PostGen

trait AccessTokenPublisherGen {
  val accessTokenGen: Gen[AccessTokenPublisherToken] =
    stringRefinedNonEmptyGen.map(AccessTokenPublisherToken(_))

  val temporaryOauthCodeGen: Gen[AccessTokenPublisherTemporaryOauthCode] =
    stringRefinedNonEmptyGen.map(AccessTokenPublisherTemporaryOauthCode(_))

  val accessTokenPublisherGen: Gen[AccessTokenPublisher] =
    for {
      accessToken <- accessTokenGen
      temporaryOauthCode <- temporaryOauthCodeGen
    } yield AccessTokenPublisher(accessToken, temporaryOauthCode)
}

trait BotGen {

  val BotIdGen: Gen[BotId] =
    stringRefinedNonEmptyGen.map(BotId(_))

  val BotNameGen: Gen[BotName] =
    stringRefinedNonEmptyGen.map(BotName(_))

  val accessTokensGen: Gen[Seq[AccessTokenPublisherToken]] =
    Gen.listOf(domain.accessTokenGen)

  val postsGen: Gen[Seq[PostId]] =
    Gen.listOf(domain.postIdGen)

  val botGen: Gen[Bot] =
    for {
      botId <- BotIdGen
      botName <- BotNameGen
      accessTokens <- accessTokensGen
      posts <- postsGen
    } yield Bot(botId, botName, accessTokens, posts)
}

trait PostGen {
  val postIdGen: Gen[PostId] =
    longRefinedPositiveGen.map(PostId(_))
}
