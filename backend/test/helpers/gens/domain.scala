package helpers.gens

import domains.workspace.WorkSpace
import domains.post.Post._
import domains.workspace.WorkSpace._
import domains.bot.Bot
import domains.bot.Bot._
import domains.post.Post.PostId
import org.scalacheck.Gen
import helpers.gens.string._
import helpers.gens.number._
import domain._
import domains.post.Post
import domains.application.Application
import domains.application.Application._
import domains.channel.{Channel, ChannelMessage, Message}
import domains.channel.Channel._
import domains.channel.ChannelMessage.{
  ChannelMessageSenderUserId,
  ChannelMessageSentAt
}

object domain extends DomainGen

trait DomainGen
    extends WorkSpaceGen with BotGen with PostGen with ApplicationGen
    with ChannelGen

trait ChannelGen {
  val channelIdGen: Gen[ChannelId] = stringRefinedNonEmptyGen.map(ChannelId(_))

  val sentAtGen: Gen[ChannelMessageSentAt] =
    refinedValidFloatGen.map(ChannelMessageSentAt(_))

  val senderUserIdGen: Gen[ChannelMessageSenderUserId] =
    stringRefinedNonEmptyGen.map(ChannelMessageSenderUserId(_))

  val channelMessageGen: Gen[ChannelMessage] = for {
    sentAt       <- sentAtGen
    senderUserId <- senderUserIdGen
    text         <- Gen.alphaStr
  } yield ChannelMessage(sentAt, senderUserId, text)

  val channelTypedChannelMessageGen: Gen[Channel] = for {
    id      <- channelIdGen
    history <- Gen.listOf(channelMessageGen)
  } yield Channel(id, history)
}

trait WorkSpaceGen {
  val temporaryOauthCodeGen: Gen[WorkSpaceTemporaryOauthCode] =
    stringRefinedNonEmptyGen.map(WorkSpaceTemporaryOauthCode(_))

  val workSpaceIdGen: Gen[WorkSpaceId] =
    stringRefinedNonEmptyGen.map(WorkSpaceId(_))

  val workSpaceGen: Gen[WorkSpace] = for {
    id                 <- workSpaceIdGen
    temporaryOauthCode <- Gen.option(temporaryOauthCodeGen)
    bots               <- Gen.listOf(botGen)
    channels           <- Gen.listOf(channelTypedChannelMessageGen)
    token              <- Gen.option(accessTokensGen)
  } yield WorkSpace(id, temporaryOauthCode, bots, channels, token)
}

trait PostGen {
  val postIdGen: Gen[PostId] = longRefinedPositiveGen.map(PostId(_))

  val postUrlGen: Gen[PostUrl] = stringRefinedUrlGen.map(PostUrl(_))

  val postTitleGen: Gen[PostTitle] = stringRefinedNonEmptyGen.map(PostTitle(_))

  val postAuthorGen: Gen[PostAuthor] =
    stringRefinedNonEmptyGen.map(PostAuthor(_))

  val postPostedAtGen: Gen[PostPostedAt] =
    longRefinedPositiveGen.map(PostPostedAt(_))

  val postGen: Gen[Post] = for {
    id       <- postIdGen
    url      <- postUrlGen
    title    <- postTitleGen
    author   <- postAuthorGen
    postedAt <- postPostedAtGen
  } yield Post(id, url, title, author, postedAt)
}

trait ApplicationGen {
  val applicationIdGen: Gen[ApplicationId] =
    stringRefinedNonEmptyGen.map(ApplicationId(_))

  val applicationNameGen: Gen[ApplicationName] =
    stringRefinedNonEmptyGen.map(ApplicationName(_))

  val applicationClientIdGen: Gen[ApplicationClientId] =
    stringRefinedNonEmptyGen.map(ApplicationClientId(_))

  val applicationClientSecretGen: Gen[ApplicationClientSecret] =
    stringRefinedNonEmptyGen.map(ApplicationClientSecret(_))

  val applicationGen: Gen[Application] = for {
    id       <- applicationIdGen
    name     <- applicationNameGen
    clientId <- Gen.option(applicationClientIdGen)
    secret   <- Gen.option(applicationClientSecretGen)
    post     <- Gen.listOf(postIdGen)
  } yield Application(id, name, clientId, secret, post)
}

trait BotGen {
  val botIdGen: Gen[BotId] = stringRefinedNonEmptyGen.map(BotId(_))

  val botNameGen: Gen[BotName] = stringRefinedNonEmptyGen.map(BotName(_))

  val accessTokensGen: Gen[BotAccessToken] =
    stringRefinedNonEmptyGen.map(BotAccessToken(_))

  val botGen: Gen[Bot] = for {
    botId        <- Gen.option(botIdGen)
    botName      <- botNameGen
    appId        <- applicationIdGen
    accessTokens <- accessTokensGen
    channels     <- Gen.listOf(channelIdGen)
  } yield Bot(botId, botName, appId, accessTokens, channels, None) // todo draft messageのgenをちゃんと作る
}

//trait MessageGen {
//  val messageIdGen: Gen[MessageId] = stringRefinedNonEmptyGen.map(MessageId(_))
//
//  val messageSentAtGen: Gen[MessageSentAt] =
//    refinedValidFloatGen.map(MessageSentAt(_))
//
//  val messageUserIdGen: Gen[MessageUserId] =
//    stringRefinedNonEmptyGen.map(MessageUserId(_))
//
//  val messageChannelIdGen: Gen[MessageChannelId] =
//    stringRefinedNonEmptyGen.map(MessageChannelId(_))
//
//  val messageGen: Gen[Message] = for {
//    id        <- messageIdGen
//    sentAt    <- messageSentAtGen
//    userId    <- messageUserIdGen
//    channelId <- messageChannelIdGen
//  } yield Message(Some(id), Some(sentAt), userId, channelId, Seq())
//}
