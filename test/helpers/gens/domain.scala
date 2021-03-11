package helpers.gens

import domains.accesstokenpublisher.AccessTokenPublisher
import domains.post.Post._
import domains.accesstokenpublisher.AccessTokenPublisher._
import domains.bot.Bot
import domains.bot.Bot._
import domains.post.Post.PostId
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

  val accessTokenPublisherGen: Gen[AccessTokenPublisher] = for {
    accessToken <- accessTokenGen
    temporaryOauthCode <- temporaryOauthCodeGen
  } yield AccessTokenPublisher(accessToken, temporaryOauthCode)
}

trait PostGen {
  val postIdGen: Gen[PostId] = longRefinedPositiveGen.map(PostId(_))

  val postUrlGen: Gen[PostUrl] = stringRefinedUrlGen.map(PostUrl(_))

  val postTitleGen: Gen[PostTitle] = stringRefinedNonEmptyGen.map(PostTitle(_))

  val postAuthorGen: Gen[PostAuthor] =
    stringRefinedNonEmptyGen.map(PostAuthor(_))

  val postPostedAtGen: Gen[PostPostedAt] =
    longRefinedPositiveGen.map(PostPostedAt(_))
}

trait BotGen {

  val botIdGen: Gen[BotId] = stringRefinedNonEmptyGen.map(BotId(_))

  val botNameGen: Gen[BotName] = stringRefinedNonEmptyGen.map(BotName(_))

  val accessTokensGen: Gen[Seq[AccessTokenPublisherToken]] =
    Gen.listOf(domain.accessTokenGen)

  val postsGen: Gen[Seq[PostId]] = Gen.listOf(domain.postIdGen)

  val botGen: Gen[Bot] = for {
    botId <- botIdGen
    botName <- botNameGen
    accessTokens <- accessTokensGen
    posts <- postsGen
  } yield Bot(botId, botName, accessTokens, posts)
}
