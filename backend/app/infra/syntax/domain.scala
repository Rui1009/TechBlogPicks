package infra.syntax

import domains.post.Post
import infra.dto.Tables._

object domain extends DomainSyntax

trait DomainSyntax {
  implicit final def infraSyntaxPost(model: Post): PostOps = new PostOps(model)
}

final private[syntax] class PostOps(private val model: Post) {
  def toRow(unixSec: Long): PostsRow = PostsRow(
    model.id.map(_.value.value).getOrElse(0),
    model.url.value.value,
    model.title.value.value,
    model.author.value.value,
    model.postedAt.value.value,
    unixSec
  )
}
