package infra.syntax

import domains.application.Application
import domains.post.UnsavedPost
import infra.dto.Tables._

object domain extends DomainSyntax

trait DomainSyntax {
  implicit final def infraSyntaxPost(model: UnsavedPost): UnsavedPostOps =
    new UnsavedPostOps(model)
  implicit final def infraSyntaxBot(model: Application): ApplicationOps  =
    new ApplicationOps(model)
}

final private[syntax] class UnsavedPostOps(private val model: UnsavedPost)
    extends AnyVal {
  def toRow(unixSec: Long): PostsRow = PostsRow(
    0,
    model.url.value.value,
    model.title.value.value,
    model.author.value.value,
    model.postedAt.value.value,
    unixSec,
    model.testimonial.map(_.value.value)
  )
}

final private[syntax] class ApplicationOps(private val model: Application)
    extends AnyVal {
  def toClientInfoRow: BotClientInfoRow = BotClientInfoRow(
    model.id.value.value,
    model.clientId.map(_.value.value),
    model.clientSecret.map(_.value.value)
  )
}
