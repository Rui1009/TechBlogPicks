package infra.syntax

import domains.bot.Bot
import domains.post.Post
import infra.dto.Tables._

object domain extends DomainSyntax

trait DomainSyntax {
  implicit final def infraSyntaxPost(model: Post): PostOps = new PostOps(model)
  implicit final def infraSyntaxBot(model: Bot): BotOps    = new BotOps(model)
}

final private[syntax] class PostOps(private val model: Post) extends AnyVal {
  def toRow(unixSec: Long): PostsRow = PostsRow(
    model.id.map(_.value.value).getOrElse(0),
    model.url.value.value,
    model.title.value.value,
    model.author.value.value,
    model.postedAt.value.value,
    unixSec
  )
}

final private[syntax] class BotOps(private val model: Bot) extends AnyVal {
  def toClientInfoRow: BotClientInfoRow = BotClientInfoRow(
    model.id.value.value,
    model.clientId.map(_.value.value),
    model.clientSecret.map(_.value.value)
  )
}
